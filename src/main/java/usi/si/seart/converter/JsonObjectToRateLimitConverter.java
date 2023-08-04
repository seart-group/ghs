package usi.si.seart.converter;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import usi.si.seart.github.RateLimit;

@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
public class JsonObjectToRateLimitConverter implements Converter<JsonObject, RateLimit> {

    JsonObjectToRateLimitResourceConverter resourceConverter;

    @Override
    @NonNull
    public RateLimit convert(@NonNull JsonObject source) {
        JsonObject resources = source.getAsJsonObject("resources");
        JsonObject core = resources.getAsJsonObject("core");
        RateLimit.Resource coreResource = resourceConverter.convert(core);
        JsonObject search = resources.getAsJsonObject("search");
        RateLimit.Resource searchResource = resourceConverter.convert(search);
        return new RateLimit(coreResource, searchResource);
    }
}
