package ch.usi.si.seart.config;

import ch.usi.si.seart.config.properties.GitProperties;
import ch.usi.si.seart.io.TemporaryDirectory;
import ch.usi.si.seart.jgit.ProxySystemReader;
import lombok.Cleanup;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.SystemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Configuration
public class GitConfig {

    @Bean
    SystemReader systemReader() throws IOException {
        SystemReader original = SystemReader.getInstance();
        File tmp = SystemUtils.getJavaIoTmpDir();
        File file = new File(tmp, ".ghs-gitconfig");
        new PrintWriter(file).close();
        SystemReader proxy = new ProxySystemReader(file, original);
        SystemReader.setInstance(proxy);
        return proxy;
    }

    @Bean
    @ConditionalOnExpression("not '${ghs.git.username}'.blank and not '${ghs.git.password}'.blank")
    CredentialsProvider credentialsProvider(GitProperties gitProperties) {
        return new UsernamePasswordCredentialsProvider(
                gitProperties.getUsername(),
                gitProperties.getPassword()
        );
    }

    @Bean
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public LsRemoteCommand lsRemoteCommand(
            GitProperties gitProperties, ObjectProvider<CredentialsProvider> credentialsProviders
    ) {
        LsRemoteCommand command = Git.lsRemoteRepository();
        credentialsProviders.ifAvailable(command::setCredentialsProvider);
        Optional.ofNullable(gitProperties.getLsRemoteTimeoutDuration())
                .map(Duration::toSeconds)
                .map(Math::toIntExact)
                .filter(timeout -> timeout > 0)
                .ifPresent(command::setTimeout);
        return command;
    }

    @Bean
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CloneCommand cloneCommand(
            GitProperties gitProperties, ObjectProvider<CredentialsProvider> credentialsProviders
    ) {
        CloneCommand command = Git.cloneRepository();
        credentialsProviders.ifAvailable(command::setCredentialsProvider);
        Optional.ofNullable(gitProperties.getCloneTimeoutDuration())
                .map(Duration::toSeconds)
                .map(Math::toIntExact)
                .filter(timeout -> timeout > 0)
                .ifPresent(command::setTimeout);
        return command;
    }

    @Bean
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public TemporaryDirectory temporaryDirectory(GitProperties gitProperties) throws IOException {
        String prefix = gitProperties.getFolderPrefix();
        return new TemporaryDirectory(prefix);
    }

    @Bean
    InitializingBean localCloneCleanupInitializingBean(GitProperties gitProperties) {
        return new InitializingBean() {

            private final Logger log = LoggerFactory.getLogger(
                    GitConfig.class.getCanonicalName() + "$LocalCloneCleanupInitializingBean"
            );

            @Override
            public void afterPropertiesSet() throws Exception {
                log.info("Cleaning up leftover local clones...");
                String prefix = gitProperties.getFolderPrefix();
                Path workdir = SystemUtils.getJavaIoTmpDir().toPath();
                @Cleanup Stream<Path> paths = Files.list(workdir);
                paths.filter(Files::isDirectory)
                        .filter(path -> path.getFileName().toString().startsWith(prefix))
                        .forEach(this::deleteRecursively);
                log.info("Finished cleaning up leftover local clones.");
            }

            private void deleteRecursively(Path path) {
                try {
                    FileSystemUtils.deleteRecursively(path);
                    log.debug("Cleaning up leftover directory: {}", path);
                } catch (IOException ex) {
                    log.error("Failed to clean up directory: {}", path, ex);
                }
            }
        };
    }

    @Bean
    InitializingBean gitConfigurationInitializingBean(SystemReader systemReader, GitProperties properties) {
        return new InitializingBean() {

            private final Logger log = LoggerFactory.getLogger(
                    GitConfig.class.getCanonicalName() + "$GitConfigurationInitializingBean"
            );

            @Override
            public void afterPropertiesSet() {
                try {
                    if (ObjectUtils.isEmpty(properties.getConfig())) return;
                    StoredConfig config = systemReader.getUserConfig();
                    afterPropertiesSet(config);
                } catch (ConfigInvalidException | IOException ex) {
                    log.error("Failed to read user configuration", ex);
                }
            }

            private void afterPropertiesSet(StoredConfig config) {
                try {
                    for (Map.Entry<String, String> configuration : properties.getConfig().entrySet()) {
                        String key = configuration.getKey();
                        String value = configuration.getValue();
                        String[] segments = key.split("\\.");
                        Assert.isTrue(segments.length == 2, "Invalid key: " + key);
                        config.setString(segments[0], null, segments[1], value);
                    }
                    config.save();
                } catch (IOException ex) {
                    log.error("Failed to save user configuration", ex);
                }
            }
        };
    }
}
