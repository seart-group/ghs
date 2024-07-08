package ch.usi.si.seart.config;

import ch.usi.si.seart.config.properties.GitProperties;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.Duration;
import java.util.Optional;

@Configuration
public class GitConfig {

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
}
