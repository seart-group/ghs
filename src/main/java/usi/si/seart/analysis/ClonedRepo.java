package usi.si.seart.analysis;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.nio.file.Path;

/**
 * A git repository cloned on the filesystem.
 */
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClonedRepo implements AutoCloseable {

    @Getter
    Path path;

    /**
     * When used in a try-with-resources block,
     * automatically deletes the folder on the
     * filesystem once the clause is exited.
     */
    @Override
    public void close() {
        File file = path.toFile();
        FileSystemUtils.deleteRecursively(file);
    }
}
