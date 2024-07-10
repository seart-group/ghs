package ch.usi.si.seart.git;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.SystemReader;

import java.io.File;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class ProxySystemReader extends SystemReader {

    File config;
    SystemReader delegate;

    @Override
    public String getHostname() {
        return delegate.getHostname();
    }

    @Override
    public String getenv(String variable) {
        return delegate.getenv(variable);
    }

    @Override
    public String getProperty(String key) {
        return delegate.getProperty(key);
    }

    @Override
    public FileBasedConfig openUserConfig(Config parent, FS fs) {
        return new FileBasedConfig(parent, config, fs);
    }

    @Override
    public FileBasedConfig openSystemConfig(Config parent, FS fs) {
        return new DummyFileConfig(parent, fs);
    }

    private static final class DummyFileConfig extends FileBasedConfig {

        DummyFileConfig(Config base, FS fs) {
            super(base, null, fs);
        }

        @Override
        public void load() {
        }

        @Override
        public boolean isOutdated() {
            return false;
        }
    }

    @Override
    public FileBasedConfig openJGitConfig(Config parent, FS fs) {
        return delegate.openJGitConfig(parent, fs);
    }

    @Override
    public long getCurrentTime() {
        return delegate.getCurrentTime();
    }

    @Override
    public int getTimezone(long when) {
        return delegate.getTimezone(when);
    }
}
