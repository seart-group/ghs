package ch.usi.si.seart.github.response;

import com.google.gson.JsonElement;

/**
 * Represents a {@link Response} obtained from GraphQL API calls.
 * Unlike {@link JsonResponse}, this class does not store the
 * status codes and headers of the response.
 *
 * @author Ozren Dabić
 */
public class GraphQlResponse extends JsonResponse {

    public GraphQlResponse(JsonElement jsonElement) {
        super(jsonElement);
    }
}
