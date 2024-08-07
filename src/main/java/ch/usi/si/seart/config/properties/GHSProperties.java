package ch.usi.si.seart.config.properties;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "ghs")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @ConstructorBinding)
public class GHSProperties {

    @NestedConfigurationProperty
    GitHubProperties github;

    @NestedConfigurationProperty
    GitProperties git;

    @NestedConfigurationProperty
    CLOCProperties cloc;

    @NestedConfigurationProperty
    CrawlerProperties crawler;

    @NestedConfigurationProperty
    AnalysisProperties analysis;

    @NestedConfigurationProperty
    CleanUpProperties cleanUp;

    public GitHubProperties getGitHubProperties() {
        return github;
    }

    public GitProperties getGitProperties() {
        return git;
    }

    public CLOCProperties getCLOCProperties() {
        return cloc;
    }

    public CrawlerProperties getCrawlerProperties() {
        return crawler;
    }

    public AnalysisProperties getAnalysisProperties() {
        return analysis;
    }

    public CleanUpProperties getCleanUpProperties() {
        return cleanUp;
    }
}
