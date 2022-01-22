package usi.si.seart.gseapp.util;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
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
            String fileName = file.getName();
            boolean successful = file.delete();
            if (!successful){
                log.error("Error could not delete file: "+fileName);
            }
        }
    }
}
