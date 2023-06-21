package usi.si.seart.job;

import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import usi.si.seart.repository.GitRepoRepository;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Tuple;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@EnableScheduling
@ConditionalOnProperty(value = "app.cleanup.enabled", havingValue = "true")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CleanUpProjectsJob {

    private static final int NO_HOST_RESOLVE = 6;
    private static final int NO_HOST_CONNECTION = 7;
    private static final int OPERATION_TIMEOUT = 28;

    EntityManagerFactory entityManagerFactory;

    GitRepoRepository gitRepoRepository;

    @NonFinal
    @Value("${spring.jpa.properties.hibernate.jdbc.fetch_size}")
    Integer fetchSize;

    @SneakyThrows(InterruptedException.class)
    @Scheduled(fixedDelayString = "${app.cleanup.scheduling}")
    public void run() {
        long totalRepositories = gitRepoRepository.count();
        log.info("Started cleanup on {} repositories...", totalRepositories);

        String sql = "SELECT id, name FROM repo ORDER BY RAND()";
        @Cleanup SessionFactory factory = entityManagerFactory.unwrap(SessionFactory.class);
        @Cleanup StatelessSession session = factory.openStatelessSession();
        @Cleanup ScrollableResults<Tuple> results = session.createNativeQuery(sql, Tuple.class)
                .setCacheable(false)
                .setFetchSize(fetchSize)
                .scroll(ScrollMode.FORWARD_ONLY);

        long totalDeleted = 0;
        while (results.next()) {
            Tuple tuple = results.get();
            Long id = tuple.get(0, Long.class);
            String name = tuple.get(1, String.class);
            log.debug("Checking if {} [id: {}] exists...", name, id);
            boolean exists = checkIfRepoExists(name);
            TimeUnit.MILLISECONDS.sleep(500);
            if (!exists) {
                log.info("Deleting repository: {} [{}]", name, id);
                Transaction transaction = null;
                try (Session nested = factory.openSession()) {
                    transaction = nested.beginTransaction();
                    nested.createNativeMutationQuery("DELETE FROM repo_label WHERE repo_id = :id")
                            .setParameter("id", id)
                            .executeUpdate();
                    nested.createNativeMutationQuery("DELETE FROM repo_language WHERE repo_id = :id")
                            .setParameter("id", id)
                            .executeUpdate();
                    nested.createNativeMutationQuery("DELETE FROM repo_metrics WHERE repo_id = :id")
                            .setParameter("id", id)
                            .executeUpdate();
                    nested.createNativeMutationQuery("DELETE FROM repo_topic WHERE repo_id = :id")
                            .setParameter("id", id)
                            .executeUpdate();
                    nested.createNativeMutationQuery("DELETE FROM repo WHERE id = :id")
                            .setParameter("id", id)
                            .executeUpdate();
                    nested.flush();
                    transaction.commit();
                } catch (PersistenceException ex) {
                    log.error("Exception occurred while deleting GitRepo [id=" + id + ", name=" + name + "]!", ex);
                    if (transaction != null) {
                        log.info("Rolling back transaction...");
                        transaction.rollback();
                    }
                }

                totalDeleted++;
            }
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
        return executeCommand("curl", "-Is", "--fail-with-body", "--show-error", "--connect-timeout", "45", url);
    }

    private boolean executeCommand(String... command) throws IOException, InterruptedException, TimeoutException {
        String joined = String.join(" ", command);
        log.trace("\tExecuting command: {}", joined);
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.environment().put("GIT_TERMINAL_PROMPT", "0");
        Process process = builder.start();

        String stderr = IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8);
        boolean exited = process.waitFor(60, TimeUnit.SECONDS);
        int returnCode;
        if (exited) {
            returnCode = process.exitValue();
        } else {
            long pid = process.pid();
            log.debug("\tProcess [{}]: Timed out! Attempting to terminate...", pid);
            while (process.isAlive()) process.destroyForcibly();
            log.debug("\tProcess [{}]: Terminated!", pid);
            returnCode = OPERATION_TIMEOUT;
        }

        switch (returnCode) {
            case 0:
                return true;
            case NO_HOST_RESOLVE:
                throw new UnknownHostException("Could not resolve host address!");
            case NO_HOST_CONNECTION:
                throw new ConnectException("Connection to host failed!");
            case OPERATION_TIMEOUT:
                throw new TimeoutException("Timed out while executing command: " + joined);
            case 130:
                throw new InterruptedException("Process terminated via SIGTERM, exit code 130.");
            default:
                log.debug("Command returned with non-zero exit code {}, stderr:\n{}", returnCode, stderr);
                return false;
        }
    }
}
