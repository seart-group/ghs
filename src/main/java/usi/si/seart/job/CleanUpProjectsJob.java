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
import usi.si.seart.projection.GitRepoView;
import usi.si.seart.repository.GitRepoRepository;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.LongFunction;

@Slf4j
@Service
@EnableScheduling
@ConditionalOnProperty(value = "app.cleanup.enabled", havingValue = "true")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CleanUpProjectsJob {

    private static final int TIMEOUT_RETURN_CODE = -3;

    GitRepoRepository gitRepoRepository;

    @Scheduled(fixedDelayString = "${app.cleanup.scheduling}")
    public void run(){
        long totalRepositories = gitRepoRepository.count();
        log.info("Started cleanup on {} repositories...", totalRepositories);

        long currentId = -1;
        long totalDeleted = 0;
        LongFunction<Optional<GitRepoView>> query = gitRepoRepository::findFirstByIdGreaterThanOrderByIdAsc;
        Optional<GitRepoView> optional = query.apply(currentId);
        while (optional.isPresent()) {
            GitRepoView view = optional.get();
            Long id = view.getId();
            String name = view.getName();
            log.debug("Checking if {} [id: {}] exists...", name, id);
            boolean exists = checkIfRepoExists(name);
            if (!exists) {
                log.info("Found repository without remote [{}], deleting...", name);
                gitRepoRepository.deleteById(id);
                gitRepoRepository.flush();
                totalDeleted++;
            }
            currentId = id;
            optional = query.apply(currentId);
        }

        log.info("Finished! {}/{} repositories deleted.", totalDeleted, totalRepositories);
    }

    /*
     * Check if a repo at given url is publicly available.
     * Technically the same code should also work for an SSH URL,
     * but in my tests SSH would trigger prompts which kill the command.
     * Prompts should remain disabled otherwise GitHub asks credentials for private repos.
     */
    private boolean checkIfRepoExists(String name) {
        String url = String.format("https://github.com/%s", name);
        try {
            return checkWithGit(url) || checkWithCURL(url);
        } catch (Exception ex) {
            // It's safer to keep projects which we fail to check, rather than removing them from DB.
            // Let's say there is a bug with our implementation, do we prefer to lose repos one by one?
            log.error("An exception has occurred during cleanup!", ex);
            return true;
        }
    }

    private boolean checkWithGit(String url) throws IOException, InterruptedException, TimeoutException {
        return executeCommand("git", "ls-remote", url, "--exit-code");
    }

    private boolean checkWithCURL(String url) throws IOException, InterruptedException, TimeoutException {
        return executeCommand("curl", "-Is", "--fail-with-body", "--show-error", url);
    }

    private boolean executeCommand(String... command) throws IOException, InterruptedException, TimeoutException {
        String joined = String.join(" ", command);
        log.trace("\tExecuting command: {}", joined);
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.environment().put("GIT_TERMINAL_PROMPT", "0");
        Process process = builder.start();

        String stderr = IOUtils.toString(process.getErrorStream());
        boolean exited = process.waitFor(60, TimeUnit.SECONDS);
        int returnCode;
        if (exited) {
            returnCode = process.exitValue();
        } else {
            long pid = process.pid();
            log.debug("\tProcess [{}]: Timed out! Attempting to terminate...", pid);
            while (process.isAlive()) process.destroyForcibly();
            log.debug("\tProcess [{}]: Terminated!", pid);
            returnCode = TIMEOUT_RETURN_CODE;
        }

        switch (returnCode) {
            case 0:
                return true;
            case TIMEOUT_RETURN_CODE:
                throw new TimeoutException("Timed out while executing command: " + joined);
            case 130:
                throw new InterruptedException("Process terminated via SIGTERM, exit code 130.");
            default:
                log.debug("Command returned with non-zero exit code {}, stderr:\n{}", returnCode, stderr);
                return false;
        }
    }
}
