package ch.usi.si.seart.job;

import ch.usi.si.seart.config.properties.CleanUpProperties;
import ch.usi.si.seart.exception.github.GitHubRestException;
import ch.usi.si.seart.github.GitHubRestConnector;
import ch.usi.si.seart.service.GitRepoService;
import ch.usi.si.seart.stereotype.Job;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Date;

@Job
@Slf4j
@ConditionalOnProperty(value = "ghs.clean-up.enabled", havingValue = "true")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class CleanUpProjectsJob implements Runnable {

    CleanUpProperties cleanUpProperties;

    ObjectFactory<LsRemoteCommand> lsRemoteCommandFactory;

    GitHubRestConnector gitHubRestConnector;

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
            boolean exists = checkIfExists(name);
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

    private boolean checkIfExists(String name) {
        try {
            return checkWithGit(name) || checkWithHTTP(name);
        } catch (HttpStatusCodeException ex) {
            log.error("An exception has occurred during cleanup! [{}]", ex.getStatusCode());
            return true;
        } catch (GitAPIException | GitHubRestException ex) {
            /*
             * It is safer to keep projects which we fail to check,
             * rather than removing them from the database.
             * Let us say there is a bug with our implementation,
             * do we prefer to lose stored entries one by one?
             */
            log.error("An exception has occurred during cleanup!", ex);
            return true;
        }
    }

    private boolean checkWithGit(String name) throws GitAPIException {
        try {
            String url = String.format("https://github.com/%s.git", name);
            lsRemoteCommandFactory.getObject().setRemote(url).call();
            return true;
        } catch (TransportException ex) {
            return false;
        }
    }

    private boolean checkWithHTTP(String name) {
        HttpStatus status = gitHubRestConnector.pingRepository(name);
        return status != HttpStatus.NOT_FOUND;
    }
}
