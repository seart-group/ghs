package usi.si.seart.converter;

import com.google.gson.JsonArray;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
public class StringToJsonArrayConverter implements Converter<String, JsonArray> {

    StringToJsonElementConverter jsonElementConverter;

    @Override
    @NotNull
    public JsonArray convert(@NotNull String source) {
        return jsonElementConverter.convert(source).getAsJsonArray();
    }
}
