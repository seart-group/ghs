package com.dabico.gseapp.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class GitRepoDtoListPaginated {
    String first;
    String prev;
    String next;
    String last;
    String csvLink;
    String jsonLink;
    long totalItems;
    long page;
    @Builder.Default
    List<GitRepoDto> items = new ArrayList<>();
}
