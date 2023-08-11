package ch.usi.si.seart.converter;

import ch.usi.si.seart.github.RateLimit;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
public class JsonObjectToRateLimitConverter implements Converter<JsonObject, RateLimit> {

    JsonObjectToRateLimitResourceConverter resourceConverter;

    @Override
    @NonNull
    public RateLimit convert(@NonNull JsonObject source) {
        JsonObject resources = source.getAsJsonObject("resources");
        JsonObject core = resources.getAsJsonObject("core");
        JsonObject search = resources.getAsJsonObject("search");
        JsonObject graphql = resources.getAsJsonObject("graphql");
        return new RateLimit(
                resourceConverter.convert(core),
                resourceConverter.convert(search),
                resourceConverter.convert(graphql)
        );
    }
}
