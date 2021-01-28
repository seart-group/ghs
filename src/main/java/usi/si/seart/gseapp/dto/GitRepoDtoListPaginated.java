package usi.si.seart.gseapp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
    String base;
    String csvLink;
    String jsonLink;
    String xmlLink;
    long totalItems;
    long totalPages;
    long page;
    @Builder.Default
    List<GitRepoDto> items = new ArrayList<>();
}
