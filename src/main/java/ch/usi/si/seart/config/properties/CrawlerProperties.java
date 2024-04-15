package ch.usi.si.seart.config.properties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.PositiveOrZero;
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

    @ConstructorBinding
    public CrawlerProperties(
            Boolean enabled,
            int minimumStars,
            List<String> languages,
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
            Date startDate
    ) {
        this.enabled = enabled;
        this.minimumStars = minimumStars;
        this.languages = languages;
        this.startDate = startDate;
    }
}
