package ch.usi.si.seart.io;

import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record TemporaryDirectory(Path path) implements AutoCloseable {

    public TemporaryDirectory(String prefix) throws IOException {
        this(Files.createTempDirectory(prefix));
    }

    public File file() {
        return path.toFile();
    }

    @Override
    public void close() throws IOException {
        FileSystemUtils.deleteRecursively(path);
    }
}
