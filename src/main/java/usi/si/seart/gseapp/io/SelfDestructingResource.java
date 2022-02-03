package usi.si.seart.gseapp.io;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * A file system resource that deletes itself after closing.
 *
 * @author dabico
 */
public class SelfDestructingResource extends FileSystemResource {

    public SelfDestructingResource(File file) {
        super(file);
    }

    @NotNull
    @Override
    public InputStream getInputStream() throws IOException {
        return new DestructingInputStream(super.getFile());
    }

    private static final class DestructingInputStream extends FileInputStream {
        private final File file;

        public DestructingInputStream(File file) throws FileNotFoundException {
            super(file);
            this.file = file;
        }

        @Override
        public void close() throws IOException {
            super.close();
            Files.delete(file.toPath());
        }
    }
}
