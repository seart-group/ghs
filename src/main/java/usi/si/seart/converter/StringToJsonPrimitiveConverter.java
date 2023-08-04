package usi.si.seart.converter;

import com.google.gson.JsonPrimitive;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
public class StringToJsonPrimitiveConverter implements Converter<String, JsonPrimitive> {

    StringToJsonElementConverter jsonElementConverter;

    @Override
    @NotNull
    public JsonPrimitive convert(@NotNull String source) {
        return jsonElementConverter.convert(source).getAsJsonPrimitive();
    }
}
