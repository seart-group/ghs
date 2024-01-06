package ch.usi.si.seart.github;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Optional;

/**
 * The abstract base class for representing GitHub API responses.
 * Regardless of API connector, each response is expected to contain JSON data.
 * This class provides methods to access and manipulate this data in various ways.
 *
 * @author Ozren Dabić
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
abstract class Response {

    JsonElement jsonElement;

    /**
     * Retrieves the encapsulated response as a JSON object.
     *
     * @return The {@code JsonObject} representation of the encapsulated response.
     */
    public JsonObject getJsonObject() {
        return jsonElement.getAsJsonObject();
    }

    /**
     * Retrieves the encapsulated response as a JSON array.
     *
     * @return The {@code JsonArray} representation of the encapsulated response.
     */
    public JsonArray getJsonArray() {
        return jsonElement.getAsJsonArray();
    }

    /**
     * Retrieves the size of the JSON element encapsulated in the response.
     * If it is neither an array nor an object, an empty optional is returned.
     *
     * @return An {@code Optional} containing the size of the {@code JsonObject} or {@code JsonArray},
     * or an empty {@code Optional} if the response is neither.
     */
    public Optional<Integer> size() {
        if (jsonElement.isJsonArray()) {
            return Optional.of(jsonElement.getAsJsonArray().size());
        } else if (jsonElement.isJsonObject()) {
            return Optional.of(jsonElement.getAsJsonObject().size());
        } else {
            return Optional.empty();
        }
    }
}
