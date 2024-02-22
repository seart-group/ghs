package ch.usi.si.seart.config.properties;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import jakarta.validation.constraints.Positive;
import java.time.Duration;

@Getter
@ConfigurationProperties(prefix = "ghs.analysis", ignoreUnknownFields = false)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @ConstructorBinding)
public class AnalysisProperties {

    Boolean enabled;

    Duration delayBetweenRuns;

    @Positive
    int maxPoolThreads;
}
