package com.dabico.gseapp.util;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import java.io.*;

public class FileSystemResourceCustom extends FileSystemResource {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemResourceCustom.class);

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
            String fileName = file.getName();
            boolean successful = file.delete();
            if (!successful){
                logger.error("Error could not delete file: "+fileName);
            }
        }
    }
}
