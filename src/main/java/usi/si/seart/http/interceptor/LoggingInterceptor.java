package usi.si.seart.http.interceptor;

import lombok.AllArgsConstructor;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class LoggingInterceptor implements Interceptor {

    private final Logger log;

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
            log.debug("<<< HTTP FAILURE", ex);
            throw ex;
        }
        long endNs = System.nanoTime();
        long ms = TimeUnit.NANOSECONDS.toMillis(endNs - startNs);
        log.debug("<<< {} ({}ms)", response.code(), ms);
        return response;
    }
}
