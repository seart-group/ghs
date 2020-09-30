package com.dabico.gseapp.github;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(makeFinal = true)
public enum Endpoints {
    DEFAULT("https://github.com"),
    API("https://api.github.com"),
    LIMIT("https://api.github.com/rate_limit"),
    REPOS("http://api.github.com/repos"),
    SEARCH("https://api.github.com/search"),
    SEARCH_REPOS("https://api.github.com/search/repositories");
    private String url;
}
