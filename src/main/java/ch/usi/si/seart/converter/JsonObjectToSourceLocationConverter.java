package ch.usi.si.seart.converter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import graphql.language.SourceLocation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JsonObjectToSourceLocationConverter implements Converter<JsonObject, SourceLocation> {

    Gson gson;

    @Override
    @NotNull
    public SourceLocation convert(@NotNull JsonObject source) {
        return gson.fromJson(source, SourceLocation.class);
    }
}
