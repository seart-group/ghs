package ch.usi.si.seart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * DTO representation for CSV exports.
 * The collection fields from GitRepoDTO are declared as Strings,
 * allowing them to be serialized in some other format (eg. JSON)
 */

@Getter
@Setter
@SuperBuilder
public class GitRepoCsvDto extends GitRepoDto {

    @JsonProperty("metrics")
    String metricsString;

    @JsonProperty("labels")
    String labelsString;

    @JsonProperty("languages")
    String languagesString;

    @JsonProperty("topics")
    String topicsString;

}
