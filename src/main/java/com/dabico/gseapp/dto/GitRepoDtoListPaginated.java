package com.dabico.gseapp.dto;

import lombok.*;

@Getter
@Setter
@Builder
public class GitRepoDtoListPaginated {
    GitRepoDtoList results;
    int totalItems;
    String next;
}
