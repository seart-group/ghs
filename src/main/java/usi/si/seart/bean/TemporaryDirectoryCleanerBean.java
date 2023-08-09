package usi.si.seart.bean;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import usi.si.seart.exception.TerminalExecutionException;
import usi.si.seart.io.ExternalProcess;

import java.nio.file.Path;

@Slf4j
@Component("TemporaryDirectoryCleanerBean")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TemporaryDirectoryCleanerBean implements InitializingBean {

    @NonFinal
    @Value("${app.git.folder-prefix}")
    String folderPrefix;

    Path tmpDir;

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            new ExternalProcess(tmpDir, "rm", "-rf", folderPrefix + "*").execute();
            log.info("Successfully cleaned up repository folder");
        } catch (TerminalExecutionException ex) {
            log.error("Failed to clean up cloned repositories from previous runs", ex);
        }
    }
}
