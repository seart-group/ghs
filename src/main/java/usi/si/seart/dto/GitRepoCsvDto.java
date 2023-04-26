package usi.si.seart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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

}
