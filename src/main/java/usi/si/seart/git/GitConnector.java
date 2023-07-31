package usi.si.seart.git;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import usi.si.seart.exception.TerminalExecutionException;
import usi.si.seart.exception.git.CloneException;
import usi.si.seart.exception.git.GitException;
import usi.si.seart.exception.git.RemoteReferenceDisplayException;
import usi.si.seart.io.ExternalProcess;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Component responsible for cloning git repositories into a temporary folder.
 */
@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GitConnector {

    @NonFinal
    @Value("${app.crawl.analysis.folder-prefix}")
    String folderPrefix;

    ConversionService conversionService;

    /**
     * Clones a git repository into a temporary folder
     * and returns a handle for the cloned repository.
     *
     * @param url the URL corresponding to the git repository.
     * @return the handle of the cloned repository.
     */
    @SuppressWarnings("ConstantConditions")
    public LocalRepositoryClone clone(URL url) throws GitException {
        try {
            Path directory = Files.createTempDirectory(folderPrefix);
            String[] command = {"git", "clone", "--quiet", "--depth", "1", url.toString(), directory.toString()};
            ExternalProcess process = new ExternalProcess(directory, command);
            log.trace("Cloning:   {}", url);
            ExternalProcess.Result result = process.execute(5, TimeUnit.MINUTES);
            if (!result.succeeded()) {
                GitException exception = conversionService.convert(result.getStdErr(), GitException.class);
                throw (GitException) exception.fillInStackTrace();
            }

            return new LocalRepositoryClone(directory);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new CloneException("Failed for: " + url, ex);
        } catch (IOException | TerminalExecutionException | TimeoutException ex) {
            throw new CloneException("Failed for: " + url, ex);
        }
    }

    /**
     * Checks the reachability of a remote repository.
     *
     * @param url the URL corresponding to the git repository.
     * @return true if the repository is public and reachable, false otherwise.
     */
    public boolean ping(URL url) throws GitException {
        try {
            String[] command = {"git", "ls-remote", url.toString(), "--exit-code"};
            ExternalProcess process = new ExternalProcess(command);
            log.trace("Pinging:   {}", url);
            ExternalProcess.Result result = process.execute(1, TimeUnit.MINUTES);
            return result.succeeded();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RemoteReferenceDisplayException("Failed for: " + url, ex);
        } catch (TerminalExecutionException | TimeoutException ex) {
            throw new RemoteReferenceDisplayException("Failed for: " + url, ex);
        }
    }
}
