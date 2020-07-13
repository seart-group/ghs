package com.dabico.gseapp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GitRepoLabelDto {
    Long id;
    String label;
}
