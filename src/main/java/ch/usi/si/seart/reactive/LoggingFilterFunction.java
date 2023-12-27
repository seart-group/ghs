package ch.usi.si.seart.reactive;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoggingFilterFunction implements ExchangeFilterFunction {

    Logger log;

    @NotNull
    @Override
    public Mono<ClientResponse> filter(@NotNull ClientRequest request, @NotNull ExchangeFunction next) {
        log.debug(">>> {} {}", request.method(), request.url());
        return next.exchange(request);
    }
}
