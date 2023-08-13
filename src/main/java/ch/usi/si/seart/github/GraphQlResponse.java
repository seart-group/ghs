package ch.usi.si.seart.github;

import com.google.gson.JsonElement;

/**
 * Represents a {@link Response} obtained from GraphQL API calls.
 *
 * @since 1.6.3
 * @author Ozren Dabić
 */
public class GraphQlResponse extends Response {

    GraphQlResponse(JsonElement jsonElement) {
        super(jsonElement);
    }
}
