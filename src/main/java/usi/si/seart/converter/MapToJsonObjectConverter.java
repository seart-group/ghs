package usi.si.seart.converter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
public class MapToJsonObjectConverter implements Converter<Map<String, Object>, JsonObject> {

    private final Gson gson;

    @Override
    @NotNull
    public JsonObject convert(@NotNull Map<String, Object> source) {
        String string = gson.toJson(source);
        return gson.fromJson(string, JsonObject.class);
    }
}
