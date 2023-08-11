package ch.usi.si.seart.converter;

import ch.usi.si.seart.github.RateLimit;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class JsonObjectToRateLimitResourceConverter implements Converter<JsonObject, RateLimit.Resource> {

    @Override
    @NotNull
    public RateLimit.Resource convert(@NotNull JsonObject source) {
        return new RateLimit.Resource(
                source.getAsJsonPrimitive("limit").getAsInt(),
                source.getAsJsonPrimitive("remaining").getAsInt(),
                source.getAsJsonPrimitive("reset").getAsLong()
        );
    }
}
