package usi.si.seart.config;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import usi.si.seart.collection.Ranges;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@AllArgsConstructor(onConstructor_ = @Autowired)
@Configuration
public class MainConfig {

    @Bean
    public Path tmpDir(@Value("${java.io.tmpdir}") String value) {
        return Path.of(value);
    }

    @Bean
    public DateTimeFormatter dateTimeFormatter() {
        return DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                .withZone(ZoneOffset.UTC);
    }

    @Bean
    public Ranges.Splitter<Date> dateRangeSplitter() {
        return new Ranges.Splitter<>((lower, upper) -> {
            Instant lowerInstant = lower.toInstant();
            Instant upperInstant = upper.toInstant();
            ZonedDateTime lowerZoned = lowerInstant.atZone(ZoneOffset.UTC);
            ZonedDateTime upperZoned = upperInstant.atZone(ZoneOffset.UTC);
            long seconds = ChronoUnit.SECONDS.between(lowerZoned, upperZoned);
            ZonedDateTime medianZoned = lowerZoned.plusSeconds(seconds / 2);
            Instant medianInstant = medianZoned.toInstant();
            return Date.from(medianInstant);
        });
    }

    @Bean
    public Ranges.Printer<Date> dateRangePrinter() {
        return new Ranges.Printer<>(date -> {
            Instant instant = date.toInstant();
            Instant truncated = instant.truncatedTo(ChronoUnit.SECONDS);
            return dateTimeFormatter().format(truncated);
        });
    }

    @Bean
    public Pageable suggestionLimitPageable(@Value("${app.statistics.suggestion-limit}") Integer limit) {
        return PageRequest.of(0, limit);
    }
}
