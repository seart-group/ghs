package usi.si.seart.converter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import graphql.language.SourceLocation;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
public class JsonObjectToSourceLocationConverter implements Converter<JsonObject, SourceLocation> {

    private final Gson gson;

    @Override
    @NotNull
    public SourceLocation convert(@NotNull JsonObject source) {
        return gson.fromJson(source, SourceLocation.class);
    }
}
