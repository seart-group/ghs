package usi.si.seart.config;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.GraphQlClient;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import usi.si.seart.github.Endpoint;
import usi.si.seart.github.GitHubTokenManager;

@Configuration
@AllArgsConstructor(onConstructor_ = @Autowired)
public class GraphQLConfig {

    GitHubTokenManager gitHubTokenManager;

    @Bean
    public GraphQlClient graphQlClient() {
        return HttpGraphQlClient.create(webClient());
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(Endpoint.GRAPH_QL.toString())
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .filter(exchangeFilterFunction())
                .build();
    }

    @Bean
    public ExchangeFilterFunction exchangeFilterFunction() {
        return new ExchangeFilterFunction() {

            @NotNull
            @Override
            public Mono<ClientResponse> filter(@NotNull ClientRequest original, @NotNull ExchangeFunction next) {
                ClientRequest modified = ClientRequest.from(original)
                        .headers(headers -> {
                            String token = gitHubTokenManager.getCurrentToken();
                            if (token != null)
                                headers.setBearerAuth(token);
                        })
                        .build();
                return next.exchange(modified);
            }
        };
    }
}
