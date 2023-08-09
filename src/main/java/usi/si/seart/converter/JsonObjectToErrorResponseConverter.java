package usi.si.seart.converter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import usi.si.seart.github.RestErrorResponse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class JsonObjectToErrorResponseConverter implements Converter<JsonObject, RestErrorResponse> {

    @Override
    @NonNull
    public RestErrorResponse convert(@NonNull JsonObject source) {
        RestErrorResponse.RestErrorResponseBuilder builder = RestErrorResponse.builder();

        builder.message(source.get("message").getAsString());

        Optional.ofNullable(source.get("documentation_url"))
                .map(element -> {
                    try {
                        return new URL(element.getAsString());
                    } catch (MalformedURLException ignored) {
                        return null;
                    }
                })
                .ifPresent(builder::documentationUrl);

        Optional.ofNullable(source.get("errors"))
                .map(JsonElement::getAsJsonArray)
                .stream()
                .flatMap(array -> StreamSupport.stream(array.spliterator(), false))
                .map(JsonElement::getAsJsonObject)
                .map(object -> {
                    String resource = Optional.ofNullable(object.get("resource"))
                            .map(JsonElement::getAsString)
                            .orElse(null);
                    String field = Optional.ofNullable(object.get("field"))
                            .map(JsonElement::getAsString)
                            .orElse(null);
                    String message = Optional.ofNullable(object.get("message"))
                            .map(JsonElement::getAsString)
                            .orElse(null);
                    String codeName = Optional.ofNullable(object.get("code"))
                            .map(JsonElement::getAsString)
                            .orElse(null);
                    return new RestErrorResponse.Error(resource, field, codeName, message);
                }).forEach(builder::error);

        return builder.build();
    }
}
