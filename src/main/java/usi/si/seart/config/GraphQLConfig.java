package usi.si.seart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.GraphQlClient;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.web.reactive.function.client.WebClient;
import usi.si.seart.github.Endpoint;

@Configuration
public class GraphQLConfig {

    @Bean
    public GraphQlClient graphQlClient() {
        WebClient webClient = WebClient.create(Endpoint.GRAPH_QL.toString());
        return HttpGraphQlClient.create(webClient);
    }
}
