package ch.usi.si.seart.git;

import ch.usi.si.seart.config.properties.GitProperties;
import ch.usi.si.seart.exception.TerminalExecutionException;
import ch.usi.si.seart.exception.git.CloneException;
import ch.usi.si.seart.exception.git.GitException;
import ch.usi.si.seart.exception.git.RemoteReferenceDisplayException;
import ch.usi.si.seart.io.ExternalProcess;
import ch.usi.si.seart.stereotype.Connector;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;

/**
 * Component responsible for interacting with the Git version control system.
 */
@Slf4j
@Connector(command = "git")
@AllArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitConnector {

    GitProperties gitProperties;

    ConversionService conversionService;

    /**
     * Clones a git repository into a temporary folder
     * and returns a handle for the cloned repository.
     *
     * @param url the URL corresponding to the git repository.
     * @return the handle of the cloned repository.
     * @throws GitException if an error occurs while executing the underlying command.
     */
    @SuppressWarnings("ConstantConditions")
    public LocalRepositoryClone clone(URL url) throws GitException {
        try {
            Path directory = Files.createTempDirectory(gitProperties.getFolderPrefix());
            String[] command = {"git", "clone", "--quiet", "--depth", "1", url.toString(), directory.toString()};
            ExternalProcess process = new ExternalProcess(directory, command);
            log.trace("Cloning:   {}", url);
            long timeout = gitProperties.getCloneTimeoutDuration().toMillis();
            ExternalProcess.Result result = process.execute(timeout);
            result.ifFailedThrow(() -> {
                GitException exception = conversionService.convert(result.getStdErr(), GitException.class);
                return (GitException) exception.fillInStackTrace();
            });
            return new LocalRepositoryClone(directory);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new CloneException("Timed out for: " + url, ex);
        } catch (IOException | TerminalExecutionException | TimeoutException ex) {
            throw new CloneException("Failed for: " + url, ex);
        }
    }

    /**
     * Checks the reachability of a remote repository.
     *
     * @param url the URL corresponding to the git repository.
     * @return true if the repository is public and reachable, false otherwise.
     * @throws GitException if an error occurs while executing the underlying command.
     */
    public boolean ping(URL url) throws GitException {
        try {
            String[] command = {"git", "ls-remote", url.toString(), "--exit-code"};
            ExternalProcess process = new ExternalProcess(command);
            log.trace("Pinging:   {}", url);
            ExternalProcess.Result result = process.execute();
            return result.succeeded();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RemoteReferenceDisplayException("Timed out for: " + url, ex);
        } catch (TerminalExecutionException | TimeoutException ex) {
            throw new RemoteReferenceDisplayException("Failed for: " + url, ex);
        }
    }
}
