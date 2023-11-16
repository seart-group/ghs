package ch.usi.si.seart.converter;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GenericStringToJsonElementConverter implements GenericConverter {

    Gson gson;

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(
                new ConvertiblePair(String.class, JsonElement.class),
                new ConvertiblePair(String.class, JsonObject.class),
                new ConvertiblePair(String.class, JsonArray.class),
                new ConvertiblePair(String.class, JsonPrimitive.class)
        );
    }

    @Override
    public Object convert(Object source, @NotNull TypeDescriptor sourceType, @NotNull TypeDescriptor targetType) {
        String input = (String) source;
        String cleaned = Strings.emptyToNull(input.trim());
        if (cleaned == null)
            return JsonNull.INSTANCE;
        return gson.fromJson(cleaned, targetType.getType());
    }
}
