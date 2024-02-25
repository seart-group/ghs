package ch.usi.si.seart.web;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ExportFormat {

    CSV(new MediaType("text", "csv")),
    JSON(MediaType.APPLICATION_JSON),
    XML(MediaType.APPLICATION_XML);

    MediaType mediaType;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
