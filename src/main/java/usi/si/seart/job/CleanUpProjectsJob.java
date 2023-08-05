package usi.si.seart.job;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usi.si.seart.exception.ClientURLException;
import usi.si.seart.exception.git.GitException;
import usi.si.seart.git.GitConnector;
import usi.si.seart.http.ClientURLConnector;
import usi.si.seart.service.GitRepoService;

import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
@Service
@ConditionalOnProperty(value = "app.cleanup.enabled", havingValue = "true")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class CleanUpProjectsJob implements Runnable {

    GitConnector gitConnector;
    ClientURLConnector curlConnector;

    GitRepoService gitRepoService;

    @Transactional(readOnly = true)
    @Scheduled(cron = "${app.cleanup.cron}")
    public void run() {
        log.info(
                "Started cleanup  on {}/{} repositories",
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
    }

    /*
     * Check if a repo at given url is publicly available.
     * Technically the same code should also work for an SSH URL,
     * but in my tests SSH would trigger prompts which kill the command.
     * Prompts should remain disabled otherwise GitHub asks credentials for private repos.
     */
    @SneakyThrows(MalformedURLException.class)
    private boolean checkIfRepoExists(String name) {
        URL url = new URL(String.format("https://github.com/%s", name));
        try {
            // try with git first and if that fails try with cURL
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
        }
    }
}
