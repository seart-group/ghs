package ch.usi.si.seart.config.properties;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Duration;

@Getter
@ConfigurationProperties(prefix = "ghs.git", ignoreUnknownFields = false)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @ConstructorBinding)
public class GitProperties {

    String username;

    String password;

    @NotBlank
    String folderPrefix;

    @NotNull
    Duration lsRemoteTimeoutDuration;

    @NotNull
    Duration cloneTimeoutDuration;
}
