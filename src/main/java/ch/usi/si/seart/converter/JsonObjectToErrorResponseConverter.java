package ch.usi.si.seart.converter;

import ch.usi.si.seart.github.response.BlockedRestErrorResponse;
import ch.usi.si.seart.github.response.DetailedRestErrorResponse;
import ch.usi.si.seart.github.response.ErrorResponse;
import ch.usi.si.seart.github.response.RestErrorResponse;
import ch.usi.si.seart.util.Dates;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
public class JsonObjectToErrorResponseConverter implements Converter<JsonObject, ErrorResponse> {

    Converter<String, URL> stringToURLConverter;

    @Override
    @NonNull
    public ErrorResponse convert(@NonNull JsonObject source) {
        if (source.size() == 0)
            throw new IllegalArgumentException("The source object is empty");
        Assert.isTrue(source.has("message"), "The source object does not contain a message");
        String message = source.get("message").getAsString();
        if (source.has("block")) {
            JsonObject item = source.get("block").getAsJsonObject();
            String reasonName = Optional.ofNullable(item.get("reason"))
                    .map(JsonElement::getAsString)
                    .orElse(null);
            Date createdAt = Optional.ofNullable(item.get("created_at"))
                    .map(JsonElement::getAsString)
                    .map(Dates::fromGitDateString)
                    .orElse(null);
            URL url = Optional.ofNullable(item.get("html_url"))
                    .map(JsonElement::getAsString)
                    .map(stringToURLConverter::convert)
                    .orElse(null);
            BlockedRestErrorResponse.Block block = new BlockedRestErrorResponse.Block(reasonName, createdAt, url);
            return BlockedRestErrorResponse.builder()
                    .message(message)
                    .block(block)
                    .build();
        } else if (source.has("errors")) {
            JsonArray array = source.get("errors").getAsJsonArray();
            List<DetailedRestErrorResponse.Error> errors = StreamSupport.stream(array.spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .map(item -> {
                        String errorResource = Optional.ofNullable(item.get("resource"))
                                .map(JsonElement::getAsString)
                                .orElse(null);
                        String errorField = Optional.ofNullable(item.get("field"))
                                .map(JsonElement::getAsString)
                                .orElse(null);
                        String errorMessage = Optional.ofNullable(item.get("message"))
                                .map(JsonElement::getAsString)
                                .orElse(null);
                        String errorCode = Optional.ofNullable(item.get("code"))
                                .map(JsonElement::getAsString)
                                .orElse(null);
                        return new DetailedRestErrorResponse.Error(errorResource, errorField, errorCode, errorMessage);
                    })
                    .toList();
            URL documentationUrl = Optional.ofNullable(source.get("documentation_url"))
                    .map(JsonElement::getAsString)
                    .map(stringToURLConverter::convert)
                    .orElse(null);
            return DetailedRestErrorResponse.builder()
                    .documentationUrl(documentationUrl)
                    .message(message)
                    .errors(errors)
                    .build();
        } else {
            return new RestErrorResponse(message);
        }
    }
}
