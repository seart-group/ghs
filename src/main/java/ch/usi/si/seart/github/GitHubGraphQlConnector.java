package ch.usi.si.seart.github;

import ch.usi.si.seart.exception.github.GitHubConnectorException;
import ch.usi.si.seart.exception.github.GitHubGraphQlException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.graphql.client.GraphQlClient;
import org.springframework.retry.RetryContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;

@Slf4j
@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitHubGraphQlConnector extends GitHubConnector<GraphQlResponse> {

    GraphQlClient graphQlClient;
    GitHubTokenManager gitHubTokenManager;
    ConversionService conversionService;

    public JsonObject getRepository(String name) {
        String[] args = name.split("/");
        if (args.length != 2)
            throw new IllegalArgumentException("Invalid repository name: " + name);
        Map<String, Object> variables = Map.of("owner", args[0], "name", args[1]);
        GraphQLCallback callback = new GraphQLCallback("repository", variables);
        GraphQlResponse response = execute(callback);
        return response.getJsonObject();
    }

    @Override
    protected GraphQlResponse execute(Callback<GraphQlResponse> callback) {
        try {
            return super.execute(callback);
        } catch (GitHubConnectorException ex) {
            throw new GitHubGraphQlException(ex.getCause());
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private final class GraphQLCallback implements Callback<GraphQlResponse> {

        String document;
        Map<String, Object> variables;

        @Override
        @SuppressWarnings("ConstantConditions")
        public GraphQlResponse doWithRetry(RetryContext context) throws RuntimeException {
            Map<String, Object> map = graphQlClient.documentName(document)
                    .variables(variables)
                    .execute()
                    .block()
                    .toMap();
            JsonObject data = conversionService.convert(map.getOrDefault("data", Map.of()), JsonObject.class);
            JsonArray errors = conversionService.convert(map.getOrDefault("errors", List.of()), JsonArray.class);
            StreamSupport.stream(errors.spliterator(), true)
                    .map(JsonElement::getAsJsonObject)
                    .findFirst()
                    .map(json -> conversionService.convert(json, GraphQlErrorResponse.class))
                    .ifPresent(errorResponse -> {
                        String name = Objects.toString(errorResponse.getErrorType(), null);
                        try {
                            GraphQlErrorResponse.ErrorType errorType = GraphQlErrorResponse.ErrorType.valueOf(name);
                            if (GraphQlErrorResponse.ErrorType.RATE_LIMITED.equals(errorType))
                                gitHubTokenManager.replaceTokenIfExpired();
                        } catch (RuntimeException ignored) {
                        }
                        throw errorResponse.asException();
                    });
            JsonObject repository = data.getAsJsonObject("repository");
            return new GraphQlResponse(repository);
        }
    }
}
