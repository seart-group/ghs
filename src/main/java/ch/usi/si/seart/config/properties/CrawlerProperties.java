package ch.usi.si.seart.config.properties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Duration;
import java.util.Date;
import java.util.List;

@Getter
@ConfigurationProperties(prefix = "ghs.crawler", ignoreUnknownFields = false)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CrawlerProperties {

    Boolean enabled;

    @PositiveOrZero
    int minimumStars;

    List<@NotBlank String> languages;

    @PastOrPresent
    Date startDate;

    @NotNull
    Duration delayBetweenRuns;

    @ConstructorBinding
    public CrawlerProperties(
            Boolean enabled,
            int minimumStars,
            List<String> languages,
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
            Date startDate,
            Duration delayBetweenRuns
    ) {
        this.enabled = enabled;
        this.minimumStars = minimumStars;
        this.languages = languages;
        this.startDate = startDate;
        this.delayBetweenRuns = delayBetweenRuns;
    }
}
