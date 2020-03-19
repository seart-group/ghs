package com.dabico.gseapp.dto;

import lombok.*;

@Getter
@Setter
@Builder
public class GitRepoLabelDto {
    Long id;
    Long repoId;
    String label;
}
