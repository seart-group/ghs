package ch.usi.si.seart.actuate.logging;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.boot.actuate.logging.LogFileWebEndpoint;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EndpointWebExtension(endpoint = LogFileWebEndpoint.class)
public class LogFileWebEndpointExtension {

    private static final String LOG_ROOT = "logs";

    LogFileWebEndpoint delegate;

    @ReadOperation(produces = "text/plain; charset=UTF-8")
    public ResponseEntity<Resource> logFile(@Selector(match = Selector.Match.ALL_REMAINING) String... segments) {
        if (segments.length == 0) return ResponseEntity.ok(delegate.logFile());
        Path path = Paths.get(LOG_ROOT, segments);
        if (Files.notExists(path)) return ResponseEntity.notFound().build();
        if (Files.isDirectory(path)) return ResponseEntity.badRequest().build();
        Resource resource = new FileSystemResource(path.toFile());
        return ResponseEntity.ok(resource);
    }
}
