package usi.si.seart.gseapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonPropertyOrder({"language", "sizeOfCode"})
public class GitRepoLanguageDto {
    @JsonIgnore
    Long id;
    @JacksonXmlProperty(localName = "name")
    String language;
    Long sizeOfCode;
}
