package ch.usi.si.seart.github;

import graphql.ErrorClassification;
import graphql.GraphqlErrorException;
import graphql.language.SourceLocation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.springframework.graphql.ResponseError;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is designed to handle GraphQL error information
 * and provide utility methods to convert and manipulate error data.
 *
 * @see ResponseError
 * @author Ozren Dabić
 */
@Builder
@Getter(onMethod_ = @Override)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GraphQlErrorResponse implements ResponseError, ErrorResponse {

    String message;
    ErrorClassification errorType;
    List<Object> parsedPath;
    List<SourceLocation> locations;
    Map<String, Object> extensions;

    @NotNull
    @Override
    public String getPath() {
        return Stream.ofNullable(parsedPath)
                .flatMap(Collection::stream)
                .map(Object::toString)
                .collect(Collectors.joining("."));
    }

    /**
     * Convert this GraphQlErrorResponse into a generic {@link GraphqlErrorException}.
     *
     * @return a new instance of {@link GraphqlErrorException}.
     */
    public GraphqlErrorException asException() {
        return GraphqlErrorException.newErrorException()
                .errorClassification(errorType)
                .sourceLocations(locations)
                .extensions(extensions)
                .path(parsedPath)
                .message(message)
                .build();
    }

    /**
     * Represents error types specific to GitHub.
     * This list is not exhaustive, and consists only
     * of cases that we have manually inspected.
     *
     * @see <a href="https://gist.github.com/dabico/bf72bb827627f0ee42623339f9c6a56d">GitHub GraphQL Error Response Examples</a>
     */
    public enum ErrorType implements ErrorClassification {

        /**
         * @see <a href="https://gist.github.com/dabico/bf72bb827627f0ee42623339f9c6a56d#rate-limit-exhausted">Example</a>
         */
        RATE_LIMITED,

        /**
         * @see <a href="https://gist.github.com/dabico/bf72bb827627f0ee42623339f9c6a56d#empty-query">Example</a>
         */
        EMPTY_QUERY,

        /**
         * @see <a href="https://gist.github.com/dabico/bf72bb827627f0ee42623339f9c6a56d#unexpected-eof">Example</a>
         */
        EARLY_EOF,

        /**
         * @see <a href="https://gist.github.com/dabico/bf72bb827627f0ee42623339f9c6a56d#query-syntax-error">Example</a>
         */
        PARSE_ERROR,

        /**
         * @see <a href="https://gist.github.com/dabico/bf72bb827627f0ee42623339f9c6a56d#non-existant-field">Example</a>
         */
        FIELD_ERROR,

        /**
         * @see <a href="https://gist.github.com/dabico/bf72bb827627f0ee42623339f9c6a56d#no-query-matches">Example</a>
         */
        NOT_FOUND,

        /**
         * @see <a href="https://gist.github.com/dabico/bf72bb827627f0ee42623339f9c6a56d#argument-type-missmatch">Example</a>
         */
        ARGUMENT_TYPE_ERROR,

        /**
         * @see <a href="https://gist.github.com/dabico/bf72bb827627f0ee42623339f9c6a56d#missing-required-argument">Example</a>
         */
        ARGUMENT_MISSING_ERROR,

        /**
         * @see <a href="https://gist.github.com/dabico/bf72bb827627f0ee42623339f9c6a56d#unacceptable-argument">Example</a>
         */
        ARGUMENT_UNKNOWN_ERROR,

        /**
         * @see <a href="https://gist.github.com/dabico/bf72bb827627f0ee42623339f9c6a56d#invalid-variable-value">Example</a>
         */
        VARIABLE_VALUE_ERROR,

        /**
         * @see <a href="https://gist.github.com/dabico/bf72bb827627f0ee42623339f9c6a56d#declared-variable-not-unused">Example</a>
         */
        VARIABLE_UNUSED_ERROR,
    }
}
