package ch.usi.si.seart.github;

import lombok.experimental.UtilityClass;
import org.springframework.http.MediaType;

@UtilityClass
public class GitHubMediaTypes {

    public static final MediaType APPLICATION_VND_GITHUB_V3_JSON;

    public static final String APPLICATION_VND_GITHUB_V3_JSON_VALUE;

    static {
        String type = "application";
        String subtype = "vnd.github.v3+json";
        APPLICATION_VND_GITHUB_V3_JSON = new MediaType(type, subtype);
        APPLICATION_VND_GITHUB_V3_JSON_VALUE = type + "/" + subtype;
    }
}
