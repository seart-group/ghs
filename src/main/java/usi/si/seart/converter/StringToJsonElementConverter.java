package usi.si.seart.converter;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToJsonElementConverter implements Converter<String, JsonElement> {

    @Override
    @NotNull
    public JsonElement convert(@NotNull String source) {
        return JsonParser.parseString(source);
    }
}
