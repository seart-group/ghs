package ch.usi.si.seart.bean.init;

import ch.usi.si.seart.exception.TerminalExecutionException;
import ch.usi.si.seart.io.ExternalProcess;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Slf4j
@Component("TemporaryDirectoryCleanerBean")
@AllArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TemporaryDirectoryCleanerBean implements InitializingBean {

    @Value("${app.git.folder-prefix}")
    String folderPrefix;

    Path tmpDir;

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            new ExternalProcess(tmpDir, "rm", "-rf", folderPrefix + "*").execute().ifFailedThrow();
            log.info("Successfully cleaned up repository folder");
        } catch (TerminalExecutionException ex) {
            log.warn("Failed to clean up cloned repositories from previous runs", ex);
        }
    }
}
