package ch.usi.si.seart.git;


import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.nio.file.Path;

/**
 * A git repository cloned on the filesystem.
 * This wrapper was introduced to more conveniently
 * clean up the cloned files once operations finished.
 */
public record LocalRepositoryClone(Path path) implements AutoCloseable {

    @Override
    public void close() {
        File file = path.toFile();
        FileSystemUtils.deleteRecursively(file);
    }
}
