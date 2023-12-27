package ch.usi.si.seart.http.interceptor;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoggingInterceptor implements Interceptor {

    Logger log;

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        log.debug(">>> {} {}", request.method(), request.url().url());
        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception ex) {
            if (log.isDebugEnabled())
                log.error("<<< HTTP FAILURE", ex);
            throw ex;
        }
        long endNs = System.nanoTime();
        long ms = TimeUnit.NANOSECONDS.toMillis(endNs - startNs);
        log.debug("<<< {} ({}ms)", response.code(), ms);
        return response;
    }
}
