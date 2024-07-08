package ch.usi.si.seart.config;

import ch.usi.si.seart.config.properties.GitProperties;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
