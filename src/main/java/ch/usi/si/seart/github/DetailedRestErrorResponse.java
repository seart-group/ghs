package ch.usi.si.seart.github;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DetailedRestErrorResponse extends RestErrorResponse {

    @Builder.Default
    URL documentationUrl = null;

    @Builder.Default
    List<Error> errors = new ArrayList<>();

    @Override
    public String toString() {
        String causes = errors.stream()
                .map(error -> "\"" + error + "\"")
                .collect(
                        () -> new StringJoiner(",", ": ", "").setEmptyValue(""),
                        StringJoiner::add,
                        StringJoiner::merge
                ).toString();
        return message + causes;
    }

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static final class Error {

        String resource;
        String field;
        Code code;
        String message;

        public Error(String resource, String field, String codeName, String message) {
            this.resource = resource;
            this.field = field;
            Code code;
            try {
                code = Code.valueOf(codeName.toUpperCase());
            } catch (RuntimeException ignored) {
                code = Code.UNKNOWN;
            }
            this.code = code;
            this.message = message;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(code.description);
            if (StringUtils.isNotBlank(message))
                builder.append(": ").append(message);
            return builder.toString();
        }

        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
        private enum Code {

            MISSING("Resource does not exist"),
            MISSING_FIELD("Field required by resource was not set"),
            INVALID("Field formatting is invalid"),
            ALREADY_EXISTS("Another resource has the same value as this field"),
            UNPROCESSABLE("Provided inputs are invalid"),
            UNKNOWN("Unknown error code was returned");

            String description;
        }
    }
}
