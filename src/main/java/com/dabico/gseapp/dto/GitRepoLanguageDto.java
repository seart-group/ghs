package com.dabico.gseapp.dto;

import lombok.*;

@Getter
@Setter
@Builder
public class GitRepoLanguageDto {
    Long id;
    Long repoId;
    String language;
    Long sizeOfCode;
}
