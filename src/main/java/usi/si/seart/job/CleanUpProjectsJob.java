package usi.si.seart.job;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import usi.si.seart.repository.GitRepoRepository;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@EnableScheduling
@ConditionalOnProperty(value = "app.cleanup.enabled", havingValue = "true")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CleanUpProjectsJob {

    // It's safer to keep projects which we fail to check, rather than removing them from DB.
    // Let's say there is a bug with our implementation, do we prefer to lose repos one by one?
    private static final int TIMEOUT_RETURN_CODE = -3;

    GitRepoRepository gitRepoRepository;

    @Scheduled(fixedDelayString = "${app.cleanup.scheduling}")
    public void run(){
        long totalRepositories = gitRepoRepository.count();
        log.info("Started cleanup on {} repositories...", totalRepositories);

        List<String> names = gitRepoRepository.findAllRepoNames();
        AtomicLong totalDeleted = new AtomicLong();
        for (String name : names) {
            String url = String.format("https://github.com/%s", name);
            boolean exists = checkIfRepoExists(url);
            if (!exists) {
                log.info("Found repository without remote [{}], deleting...", name);
                gitRepoRepository.findGitRepoByName(name.toLowerCase()).ifPresent(gitRepo -> {
                    gitRepoRepository.delete(gitRepo);
                    totalDeleted.getAndIncrement();
                });
            }
        }

        log.info("Finished! {}/{} repositories deleted.", totalRepositories, totalDeleted);
    }


    /*
     * Check if a repo at given url is publicly available.
     * Technically the same code should also work for an SSH URL,
     * but in my tests SSH would trigger prompts which kill the command.
     * Prompts should remain disabled otherwise GitHub asks credentials for private repos.
     */
    private boolean checkIfRepoExists(String url) {
        boolean exists = true;
        try {
            ProcessBuilder builder = new ProcessBuilder("git", "ls-remote", url, "--exit-code");
            builder.environment().put("GIT_TERMINAL_PROMPT", "0");
            Process process = builder.start();
            String stderr = IOUtils.toString(process.getErrorStream());
            boolean exited = process.waitFor(60, TimeUnit.SECONDS);
            log.debug("Command stderr:\n{}", stderr);
            int returnCode;
            if (exited) {
                returnCode = process.exitValue();
            } else {
                long pid = process.pid();
                log.warn("Process [{}]: Timed out! Attempting to terminate...", pid);
                while (process.isAlive()) process.destroyForcibly();
                log.info("Process [{}]: Terminated!", pid);
                returnCode = TIMEOUT_RETURN_CODE;
            }
            exists = returnCode <= 0;
        } catch (Exception ex) {
            log.error("An exception has occurred during cleanup!", ex);
        }

        return exists;
    }
}
