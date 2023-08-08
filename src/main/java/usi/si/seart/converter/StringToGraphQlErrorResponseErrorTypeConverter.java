package usi.si.seart.converter;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import usi.si.seart.github.GraphQlErrorResponse;

import java.util.regex.Pattern;

@Component
public class StringToGraphQlErrorResponseErrorTypeConverter implements Converter<String, GraphQlErrorResponse.ErrorType>
{
    // https://www.debuggex.com/r/kzZ6PfNvkQLAg3qG
    private static final Pattern RATE_LIMITED_PATTERN = Pattern.compile(
            "^API rate limit exceeded for user ID (\\d+)\\.$"
    );

    // https://www.debuggex.com/r/1unTNcRjYb3M8TrH
    private static final Pattern FIELD_ERROR_PATTERN = Pattern.compile(
            "^Field '([^']+)' doesn't exist on type '([^']+)'$"
    );

    // https://www.debuggex.com/r/yK-tNA559lGPF0aL
    private static final Pattern NOT_FOUND_PATTERN = Pattern.compile(
            "^Could not resolve to an? (\\w+) with the (\\w+) (?:of )?'([^']+)'\\.$"
    );

    // https://www.debuggex.com/r/6BjTNEeyleQXfBjx
    private static final Pattern ARGUMENT_TYPE_ERROR_PATTERN = Pattern.compile(
            "^Argument '([^']+)' on Field '([^']+)' has an invalid value \\(([^)]+)\\). Expected type '([^']+)'\\.$"
    );

    // https://www.debuggex.com/r/mcZ2rcy61FSqVpBO
    private static final Pattern ARGUMENT_MISSING_ERROR_PATTERN = Pattern.compile(
            "^Field '([^']+)' is missing required arguments?: (\\w+(?:,\\s*\\w+)*)$"
    );

    // https://www.debuggex.com/r/emiQbdZlfCYhAlhi
    private static final Pattern ARGUMENT_UNKNOWN_ERROR_PATTERN = Pattern.compile(
            "^Field '([^']+)' doesn't accept argument '([^']+)'$"
    );

    // https://www.debuggex.com/r/_WAiksCCoBiSq9nw
    private static final Pattern VARIABLE_VALUE_ERROR_PATTERN = Pattern.compile(
            "^Variable \\$(\\w+) of type (\\w+!?) was provided invalid value$"
    );

    // https://www.debuggex.com/r/OqQixKpVA8MogqMH
    private static final Pattern VARIABLE_UNUSED_ERROR_PATTERN = Pattern.compile(
            "^Variable \\$(\\w+) is declared by (\\w+)(?: query)? but not used$"
    );

    @Override
    @Nullable
    public GraphQlErrorResponse.ErrorType convert(@NotNull String source) {
        if (source.equals("A query attribute must be specified and must be a string."))
            return GraphQlErrorResponse.ErrorType.EMPTY_QUERY;
        if (source.equals("Unexpected end of document"))
            return GraphQlErrorResponse.ErrorType.EARLY_EOF;
        if (source.startsWith("Parse error"))
            return GraphQlErrorResponse.ErrorType.PARSE_ERROR;
        if (RATE_LIMITED_PATTERN.matcher(source).matches())
            return GraphQlErrorResponse.ErrorType.RATE_LIMITED;
        if (FIELD_ERROR_PATTERN.matcher(source).matches())
            return GraphQlErrorResponse.ErrorType.FIELD_ERROR;
        if (NOT_FOUND_PATTERN.matcher(source).matches())
            return GraphQlErrorResponse.ErrorType.NOT_FOUND;
        if (ARGUMENT_TYPE_ERROR_PATTERN.matcher(source).matches())
            return GraphQlErrorResponse.ErrorType.ARGUMENT_TYPE_ERROR;
        if (ARGUMENT_MISSING_ERROR_PATTERN.matcher(source).matches())
            return GraphQlErrorResponse.ErrorType.ARGUMENT_MISSING_ERROR;
        if (ARGUMENT_UNKNOWN_ERROR_PATTERN.matcher(source).matches())
            return GraphQlErrorResponse.ErrorType.ARGUMENT_UNKNOWN_ERROR;
        if (VARIABLE_VALUE_ERROR_PATTERN.matcher(source).matches())
            return GraphQlErrorResponse.ErrorType.VARIABLE_VALUE_ERROR;
        if (VARIABLE_UNUSED_ERROR_PATTERN.matcher(source).matches())
            return GraphQlErrorResponse.ErrorType.VARIABLE_UNUSED_ERROR;
        return null;
    }
}
