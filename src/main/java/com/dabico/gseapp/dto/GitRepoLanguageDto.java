package com.dabico.gseapp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GitRepoLanguageDto {
    Long id;
    String language;
    Long sizeOfCode;
}
