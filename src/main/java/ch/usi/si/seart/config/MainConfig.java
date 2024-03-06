package ch.usi.si.seart.config;

import ch.usi.si.seart.config.properties.StatisticsProperties;
import ch.usi.si.seart.util.Ranges;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Configuration
public class MainConfig {

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneOffset.UTC);
        return new Ranges.Printer<>(date -> {
            Instant instant = date.toInstant();
            Instant truncated = instant.truncatedTo(ChronoUnit.SECONDS);
            return formatter.format(truncated);
        });
    }

    @Bean
    public Pageable suggestionLimitPageable(StatisticsProperties properties) {
        int pageSize = properties.getSuggestionLimit();
        if (pageSize == 0) return Pageable.unpaged();
        return PageRequest.ofSize(pageSize);
    }
}
