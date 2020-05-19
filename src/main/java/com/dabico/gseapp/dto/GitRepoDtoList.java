package com.dabico.gseapp.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@JacksonXmlRootElement(localName = "GitRepoList")
public class GitRepoDtoList {
    @Builder.Default
    @JacksonXmlElementWrapper(localName = "items")
    @JacksonXmlProperty(localName = "GitRepo")
    List<GitRepoDto> items = new ArrayList<>();
}
