package usi.si.seart.gseapp.github_service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public enum Endpoints {
    DEFAULT("https://github.com"),
    API("https://api.github.com"),
    LIMIT("https://api.github.com/rate_limit"),
    REPOS("https://api.github.com/repos"),
    SEARCH("https://api.github.com/search"),
    SEARCH_REPOS("https://api.github.com/search/repositories");
    String url;
}
