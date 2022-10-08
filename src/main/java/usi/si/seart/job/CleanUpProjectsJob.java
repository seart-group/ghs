package usi.si.seart.job;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import usi.si.seart.model.GitRepo;
import usi.si.seart.repository.GitRepoRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@EnableScheduling
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CleanUpProjectsJob {

    private static final int RUN_COMMAND_TIMEOUT = -3;

    GitRepoRepository gitRepoRepository;

    @Scheduled(fixedDelayString = "${app.cleanup.scheduling}")
    public void run(){
        log.info("CleanUpProjectsJob started ...");
        List<String> allRepos = gitRepoRepository.findAllRepoNames();

        final int totalRepos = allRepos.size();
        log.info("CleanUpProjectsJob started on {} repositories ...", totalRepos);

        int totalDeleted = 0;
        for (int i = 0; i < allRepos.size(); i++) {
            String repo = allRepos.get(i);
            String repoURL = String.format("https://github.com/%s", repo);
            boolean exists = checkIfRepoExists(repoURL);
            if (!exists) {
                log.info("{}/{}\tChecking if repo exists: {} ==> TO BE DELETED", i, totalRepos, repoURL);
                Optional<GitRepo> optional = gitRepoRepository.findGitRepoByName(repo.toLowerCase());
                if (optional.isPresent()) {
                    GitRepo existing = optional.get();
                    gitRepoRepository.delete(existing);
                    totalDeleted++;
                }
            }
        }

        log.info("CleanUpProjectsJob finished on {} repositories. {} DELETED.", totalRepos, totalDeleted);
    }


    /**
     * Check if a repo at given url is publicly available.
     * @param repoUrl The http url of the repo. Technically the same code should also work for ssh url, but in my tests,
     *                ssh prompts (for adding fingerprint, whatsoever) which kills the command as we disabled prompts.
     *                (Prompts should remain disabled, otherwise GitHub asks user/pass for private repos)
     */
    private boolean checkIfRepoExists(String repoUrl) {
        boolean res;
        try {
            ProcessBuilder pb = new ProcessBuilder("git", "ls-remote", repoUrl);
            pb.environment().put("GIT_TERMINAL_PROMPT", "0");
            Process process = pb.start();

            InputStreamConsumerThread inputConsumer = new InputStreamConsumerThread(process.getInputStream());
            InputStreamConsumerThread errorConsumer = new InputStreamConsumerThread(process.getErrorStream());
            inputConsumer.start();
            errorConsumer.start();

            boolean noTimeout = process.waitFor(60, TimeUnit.SECONDS);
            int returnCode;
            if (noTimeout) {
                returnCode = process.exitValue();
            } else {
                long pid = process.pid();
                log.error("Attempting to terminate timed-out process: [{}] ...", pid);
                while (process.isAlive()) process.destroyForcibly();
                log.info("Process [{}] terminated!", pid);
                returnCode = RUN_COMMAND_TIMEOUT;
            }

            // Why also RUN_COMMAND_TIMEOUT? Because it's safer to keep projects which we fail to check, than removing them
            // from db. Let's say there is a bug with our implementation, do we prefer to lose repos one by one? nope.
            // Once the code is reviewed, we can drop the "res == RUN_COMMAND_TIMEOUT" part.
            res = (returnCode == 0 || returnCode == RUN_COMMAND_TIMEOUT);
        } catch (Exception ex) {
            log.error("An exception has occurred during cleanup!", ex);
            res = true; // We return "true" to prevent deleting repos from GHS database in case of errors
        }

        return res;
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class InputStreamConsumerThread extends Thread {
        private final InputStream is;

        @Override
        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                for (String line = br.readLine(); line != null; line = br.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
