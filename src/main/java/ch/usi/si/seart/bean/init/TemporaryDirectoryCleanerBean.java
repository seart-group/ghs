package ch.usi.si.seart.bean.init;

import ch.usi.si.seart.exception.TerminalExecutionException;
import ch.usi.si.seart.io.ExternalProcess;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.nio.file.Path;

@Slf4j
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TemporaryDirectoryCleanerBean implements InitializingBean {

    Path workdir;

    String prefix;

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            new ExternalProcess(workdir, "rm", "-rf", prefix + "*").execute().ifFailedThrow();
            log.info("Successfully cleaned up repository folder");
        } catch (TerminalExecutionException ex) {
            log.warn("Failed to clean up cloned repositories from previous runs", ex);
        }
    }
}
