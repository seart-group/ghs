package ch.usi.si.seart.git;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.nio.file.Path;

/**
 * A git repository cloned on the filesystem.
 * This wrapper was introduced to more conveniently
 * clean up the cloned files once operations finished.
 */
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocalRepositoryClone implements AutoCloseable {

    Path path;

    @Override
    public void close() {
        File file = path.toFile();
        FileSystemUtils.deleteRecursively(file);
    }
}
