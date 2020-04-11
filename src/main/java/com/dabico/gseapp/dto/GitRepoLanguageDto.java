package com.dabico.gseapp.dto;

import lombok.*;

@Getter
@Setter
@Builder
public class GitRepoLanguageDto {
    Long id;
    String language;
    Long sizeOfCode;
}
