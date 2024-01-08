package ch.usi.si.seart.github;

import com.google.gson.JsonElement;

/**
 * Represents a {@link JsonResponse} obtained from GraphQL API calls.
 *
 * @author Ozren Dabić
 */
public class GraphQlResponse extends JsonResponse {

    GraphQlResponse(JsonElement jsonElement) {
        super(jsonElement);
    }
}
