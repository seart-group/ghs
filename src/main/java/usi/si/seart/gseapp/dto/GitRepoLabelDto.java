package usi.si.seart.gseapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GitRepoLabelDto {
    @JsonIgnore
    Long id;
    @JacksonXmlProperty(localName = "name")
    String label;
}
