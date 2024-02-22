package ch.usi.si.seart.config.properties;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.scheduling.support.CronTrigger;

import jakarta.validation.constraints.NotNull;

@Getter
@ConfigurationProperties(prefix = "ghs.clean-up", ignoreUnknownFields = false)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @ConstructorBinding)
public class CleanUpProperties {

    Boolean enabled;

    @NotNull
    CronTrigger cron;
}
