package usi.si.seart.gseapp.util;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class FileSystemResourceCustom extends FileSystemResource {

    public FileSystemResourceCustom(File file) {
        super(file);
    }

    @NotNull
    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStreamCustom(super.getFile());
    }

    private static final class FileInputStreamCustom extends FileInputStream {
        private final File file;

        public FileInputStreamCustom(File file) throws FileNotFoundException {
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
