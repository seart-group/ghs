package usi.si.seart.gseapp.job;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import usi.si.seart.gseapp.model.GitRepo;
import usi.si.seart.gseapp.repository.GitRepoRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@EnableScheduling
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CleanUpProjectsJob {

    private static final int RUN_COMMAND_TIMEOUT = -3;

    @NonFinal boolean running = false;
    GitRepoRepository gitRepoRepository;

    @Autowired
    public CleanUpProjectsJob(GitRepoRepository gitRepoRepository)
    {
        this.gitRepoRepository = gitRepoRepository;
    }

//  @EventListener(ApplicationReadyEvent.class) // instead of scheduling, this line runs the method once server is up
    @Scheduled(fixedRateString = "#{@applicationPropertyServiceImpl.getCleanUpScheduling()}")
    public void run(){
        if (this.running) {
            log.error("CleanUpProjectsJob wanted to run while the prior job still running!!!!");
            return;
        }
        this.running = true;
        CleanUp();
        this.running = false;
    }

    private void CleanUp() {
        log.info("CleanUpProjectsJob started ....");
        List<String> allRepos = gitRepoRepository.findAllRepoNames();

        final int TOTAL = allRepos.size();
        log.info("CleanUpProjectsJob started on {} repositories ...", TOTAL);

        int cur=0, nDeleted=0;
        for(String repo: allRepos)
        {
            cur++;
            String repoURL = String.format("https://github.com/%s", repo);
//            logger.debug("{}/{}\tChecking if repo exists: {}",cur, TOTAL, repoURL);
            boolean exists = CheckIfRepoExists(repoURL);
            if(exists==false) {
                log.info("{}/{}\tChecking if repo exists: {} ==> TO BE DELETED", cur, TOTAL, repoURL);
                Optional<GitRepo> opt = gitRepoRepository.findGitRepoByName(repo.toLowerCase());
                if (opt.isPresent()) {
                    GitRepo existing = opt.get();
                    gitRepoRepository.delete(existing);
                    nDeleted++;
                }
            }
        }
        log.info("CleanUpProjectsJob finished on {} repositories. {} DELETED.", TOTAL, nDeleted);
    }



    /**
     * Check if a repo at given url is publicly available.
     * @param repo_http_url The http url of the repo. Technically the same code should also work for ssh url, but in my
     *                      tests, ssh prompts (for adding fingerprint, whatsoever) which kills the command as we disabled
     *                      prompts. (Prompts should remain disabled, otherwise GitHub asks user/pass for private repos)
     */
    private boolean CheckIfRepoExists(String repo_http_url)
    {
        boolean res;
        try {
            String[] cmd_array = List.of("git", "ls-remote", repo_http_url).toArray(new String[0]);
            ProcessBuilder pb = new ProcessBuilder(cmd_array);
            pb.environment().put("GIT_TERMINAL_PROMPT", "0");
            Process process = pb.start();

            InputStreamConsumerThread inputConsumer = new InputStreamConsumerThread(process.getInputStream());
            InputStreamConsumerThread errorConsumer = new InputStreamConsumerThread(process.getErrorStream());
            inputConsumer.start();
            errorConsumer.start();

            boolean noTimeout = process.waitFor(60, TimeUnit.SECONDS);
            int returnCode = 0;
            if(noTimeout)
                returnCode = process.exitValue();
            else {
                while(process.isAlive()) {
                    System.err.println("Trying to kill timed-out process "+process.pid()+" ...");
                    process.destroyForcibly();
                }
                returnCode =  RUN_COMMAND_TIMEOUT;
            }

            // Why also RUN_COMMAND_TIMEOUT? Because it's safer to keep projects which we fail to check, than removing them
            // from db. Let's say there is a bug with our implementation, do we prefer to lose repos one by one? nope.
            // Once the code is reviewed, we can drop the "res == RUN_COMMAND_TIMEOUT" part.
            res = (returnCode == 0 || returnCode == RUN_COMMAND_TIMEOUT);
        } catch (Exception e) {
            System.err.println("Exception: "+e);
            e.printStackTrace();
            res = true; // We return "true" to prevent deleting repos from GHS database in case of errors
        }

        return res;
    }

    public static class InputStreamConsumerThread extends Thread
    {
        private final InputStream is;

        public InputStreamConsumerThread (InputStream is)
        {
            this.is=is;
        }

        public void run()
        {
            try(BufferedReader br = new BufferedReader(new InputStreamReader(is)))
            {
                for (String line = br.readLine(); line != null; line = br.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
