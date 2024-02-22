package ch.usi.si.seart.reactive;

import ch.usi.si.seart.github.GitHubHttpHeaders;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoggingFilterFunction implements ExchangeFilterFunction {

    Logger log;

    @NotNull
    @Override
    public Mono<ClientResponse> filter(@NotNull ClientRequest request, @NotNull ExchangeFunction next) {
        log.debug(">>> {} {}", request.method(), request.url());
        long startNs = System.nanoTime();
        return next.exchange(request)
                .doOnSuccess(response -> {
                    long endNs = System.nanoTime();
                    long ms = TimeUnit.NANOSECONDS.toMillis(endNs - startNs);
                    log.debug("<<< {} ({}ms)", response.statusCode().value(), ms);
                    HttpHeaders headers = response.headers().asHttpHeaders();
                    String id = headers.getFirst(GitHubHttpHeaders.X_GITHUB_REQUEST_ID);
                    log.trace("[[[ {} ]]]", id);
                })
                .doOnError(ex -> {
                    if (log.isDebugEnabled())
                        log.error("<<< HTTP FAILURE", ex);
                });
    }
}
