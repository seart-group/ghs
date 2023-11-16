package ch.usi.si.seart.converter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MapToJsonObjectConverter implements Converter<Map<String, ?>, JsonObject> {

    Gson gson;

    @Override
    @NotNull
    public JsonObject convert(@NotNull Map<String, ?> source) {
        String string = gson.toJson(source);
        return gson.fromJson(string, JsonObject.class);
    }
}
