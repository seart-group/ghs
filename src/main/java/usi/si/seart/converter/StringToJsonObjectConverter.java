package usi.si.seart.converter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.util.Objects;

@AllArgsConstructor
public class StringToJsonObjectConverter implements Converter<String, JsonObject> {

    Gson gson;

    @Override
    @NonNull
    public JsonObject convert(@NonNull String source) {
        JsonObject result = gson.fromJson(source, JsonObject.class);
        return Objects.requireNonNullElseGet(result, JsonObject::new);
    }
}
