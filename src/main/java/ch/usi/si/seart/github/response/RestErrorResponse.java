package ch.usi.si.seart.github.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter(onMethod_ = @Override)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PROTECTED)
public class RestErrorResponse implements ErrorResponse {

    String message;

    @Override
    public String toString() {
        return message;
    }
}
