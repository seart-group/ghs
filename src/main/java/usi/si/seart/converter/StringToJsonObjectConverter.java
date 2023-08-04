package usi.si.seart.converter;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
public class StringToJsonObjectConverter implements Converter<String, JsonObject> {

    StringToJsonElementConverter jsonElementConverter;

    @Override
    @NonNull
    public JsonObject convert(@NonNull String source) {
        return jsonElementConverter.convert(source).getAsJsonObject();
    }
}
