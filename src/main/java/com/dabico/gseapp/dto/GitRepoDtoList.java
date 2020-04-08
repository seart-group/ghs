package com.dabico.gseapp.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class GitRepoDtoList {
    @Builder.Default
    List<GitRepoDto> items = new ArrayList<>();
}
