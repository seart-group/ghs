package usi.si.seart.converter;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import graphql.ErrorClassification;
import graphql.language.SourceLocation;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import usi.si.seart.github.GraphQlErrorResponse;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
public class JsonObjectToGraphQlErrorResponse implements Converter<JsonObject, GraphQlErrorResponse> {

    Gson gson;
    JsonObjectToSourceLocationConverter sourceLocationConverter;
    StringToGraphQlErrorResponseErrorTypeConverter errorTypeConverter;

    @Override
    @NotNull
    public GraphQlErrorResponse convert(@NotNull JsonObject source) {
        GraphQlErrorResponse.GraphQlErrorResponseBuilder builder = GraphQlErrorResponse.builder();

        String message = source.getAsJsonPrimitive("message").getAsString();
        builder.message(message);
        ErrorClassification errorType = errorTypeConverter.convert(message);
        builder.errorType(errorType);

        if (source.has("path")) {
            JsonArray array = source.getAsJsonArray("path");
            Type type = new TypeToken<List<Object>>() { }.getType();
            List<Object> parsedPath = gson.fromJson(array, type);
            builder.parsedPath(parsedPath);
        }

        if (source.has("locations")) {
            JsonArray array = source.getAsJsonArray("locations");
            List<SourceLocation> locations = StreamSupport.stream(array.spliterator(), true)
                    .map(JsonElement::getAsJsonObject)
                    .map(sourceLocationConverter::convert)
                    .collect(Collectors.toList());
            builder.locations(locations);
        }

        if (source.has("extensions")) {
            JsonObject object = source.getAsJsonObject("extensions");
            Type type = new TypeToken<Map<String, Object>>() { }.getType();
            Map<String, Object> extensions = gson.fromJson(object, type);
            builder.extensions(extensions);
        }

        return builder.build();
    }
}
