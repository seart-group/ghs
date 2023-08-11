package ch.usi.si.seart.converter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ListToJsonArrayConverter implements Converter<List<?>, JsonArray> {

    private final Gson gson;

    @Override
    @NotNull
    public JsonArray convert(@NotNull List<?> source) {
        String string = gson.toJson(source);
        return gson.fromJson(string, JsonArray.class);
    }
}
