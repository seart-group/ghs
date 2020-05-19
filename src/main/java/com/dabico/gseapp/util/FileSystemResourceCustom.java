package com.dabico.gseapp.util;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.FileSystemResource;

import java.io.*;

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
        private File file;

        public FileInputStreamCustom(File file) throws FileNotFoundException {
            super(file);
            this.file = file;
        }

        @Override
        public void close() throws IOException {
            super.close();
            file.delete();
        }
    }
}
