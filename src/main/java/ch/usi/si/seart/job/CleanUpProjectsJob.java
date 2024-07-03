package ch.usi.si.seart.job;

import ch.usi.si.seart.config.properties.CleanUpProperties;
import ch.usi.si.seart.exception.ClientURLException;
import ch.usi.si.seart.exception.git.GitException;
import ch.usi.si.seart.git.GitConnector;
import ch.usi.si.seart.http.ClientURLConnector;
import ch.usi.si.seart.service.GitRepoService;
import ch.usi.si.seart.stereotype.Job;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

@Job
@Slf4j
@ConditionalOnProperty(value = "ghs.clean-up.enabled", havingValue = "true")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class CleanUpProjectsJob implements Runnable {

    CleanUpProperties cleanUpProperties;

    GitConnector gitConnector;
    ClientURLConnector curlConnector;

    GitRepoService gitRepoService;

    @Transactional(readOnly = true)
    @Scheduled(cron = "${ghs.clean-up.cron}")
    public void run() {
        log.info(
                "Started cleanup on {}/{} repositories",
                gitRepoService.countCleanupCandidates(),
                gitRepoService.count()
        );
        gitRepoService.streamCleanupCandidates().forEach(pair -> {
            Long id = pair.getFirst();
            String name = pair.getSecond();
            boolean exists = checkIfRepoExists(name);
            if (!exists) {
                log.info("Deleting:  {} [{}]", name, id);
                gitRepoService.deleteRepoById(id);
            } else {
                gitRepoService.pingById(id);
            }
        });
        CronTrigger trigger = cleanUpProperties.getCron();
        TriggerContext context = new SimpleTriggerContext();
        Date date = trigger.nextExecutionTime(context);
        log.info("Next cleanup scheduled for: {}", date);
    }

    /*
     * Check if a repo at given url is publicly available.
     * Technically the same code should also work for an SSH URL,
     * but in my tests SSH would trigger prompts which kill the command.
     * Prompts should remain disabled otherwise GitHub asks credentials for private repos.
     */
    private boolean checkIfRepoExists(String name) {
        try {
            /* Try with git first and if that fails try with cURL */
            URL url = new URL(String.format("https://github.com/%s", name));
            return gitConnector.ping(url) || curlConnector.ping(url);
        } catch (GitException | ClientURLException ex) {
            /*
             * It's safer to keep projects which we fail to check,
             * rather than removing them from the database.
             * Let's say there is a bug with our implementation,
             * do we prefer to lose stored entries one by one?
             */
            log.error("An exception has occurred during cleanup!", ex);
            return true;
        } catch (MalformedURLException ex) {
            /* Should never happen, since we control the URL. */
            throw new IllegalStateException(ex);
        }
    }
}
