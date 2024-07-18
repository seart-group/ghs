package ch.usi.si.seart.config.properties;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.util.unit.DataSize;

import javax.validation.constraints.NotNull;
import java.time.Duration;

@Getter
@ConfigurationProperties(prefix = "ghs.cloc", ignoreUnknownFields = false)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @ConstructorBinding)
public class CLOCProperties {

    @NotNull
    DataSize maxFileSize;

    @NotNull
    Duration analysisTimeoutDuration;
}
