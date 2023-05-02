package usi.si.seart.converter;

import com.google.gson.JsonObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import usi.si.seart.github.RateLimit;

public class JsonObjectToRateLimitConverter implements Converter<JsonObject, RateLimit> {

    @Override
    @NonNull
    public RateLimit convert(@NonNull JsonObject source) {
        JsonObject resources = source.getAsJsonObject("resources");

        JsonObject core = resources.getAsJsonObject("core");
        RateLimit.Resource coreResource = new RateLimit.Resource(
                core.get("limit").getAsInt(),
                core.get("remaining").getAsInt(),
                core.get("reset").getAsLong()
        );

        JsonObject search = resources.getAsJsonObject("search");
        RateLimit.Resource searchResource = new RateLimit.Resource(
                search.get("limit").getAsInt(),
                search.get("remaining").getAsInt(),
                search.get("reset").getAsLong()
        );

        return new RateLimit(coreResource, searchResource);
    }
}
