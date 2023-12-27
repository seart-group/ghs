package ch.usi.si.seart.config;

import ch.usi.si.seart.config.properties.GitHubProperties;
import ch.usi.si.seart.github.Endpoint;
import ch.usi.si.seart.github.GitHubTokenManager;
import io.netty.channel.ChannelOption;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.GraphQlClient;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class GraphQlConfig {

    @Bean
    public GraphQlClient graphQlClient(WebClient webClient) {
        return HttpGraphQlClient.create(webClient);
    }

    @Bean
    WebClient webClient(ExchangeFilterFunction exchangeFilterFunction, GitHubProperties properties) {
        return WebClient.builder()
                .baseUrl(Endpoint.GRAPH_QL.toString())
                .defaultHeader("X-GitHub-Api-Version", properties.getApiVersion())
                .filter(exchangeFilterFunction)
                .build();
    }

    @Bean
    ReactorClientHttpConnector reactorClientHttpConnector(HttpClient httpClient) {
        return new ReactorClientHttpConnector(httpClient);
    }

    @Bean
    HttpClient httpClient() {
        return HttpClient.create()
                .disableRetry(true)
                .responseTimeout(Duration.ofMinutes(1))
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60_000);
    }

    @Bean
    ExchangeFilterFunction exchangeFilterFunction(GitHubTokenManager gitHubTokenManager) {
        return new ExchangeFilterFunction() {

            @NotNull
            @Override
            public Mono<ClientResponse> filter(@NotNull ClientRequest original, @NotNull ExchangeFunction next) {
                ClientRequest modified = ClientRequest.from(original)
                        .headers(headers -> {
                            String token = gitHubTokenManager.getCurrentToken();
                            if (token != null) headers.setBearerAuth(token);
                        })
                        .build();
                return next.exchange(modified);
            }
        };
    }
}
