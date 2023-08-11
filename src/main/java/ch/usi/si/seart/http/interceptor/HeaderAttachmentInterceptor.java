package ch.usi.si.seart.http.interceptor;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class HeaderAttachmentInterceptor implements Interceptor {

    Headers headers;

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        headers.forEach(header -> {
            String name = header.getFirst();
            String value = header.getSecond();
            builder.addHeader(name, value);
        });
        Request request = builder.build();
        return chain.proceed(request);
    }
}
