package com.dabico.gseapp.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class GitRepoDtoListPaginated {
    String prev;
    String next;
    String csvLink;
    String jsonLink;
    int totalItems;
    @Builder.Default
    List<GitRepoDto> items = new ArrayList<>();
}
