package ch.usi.si.seart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.time.Instant;

@Slf4j
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(
        name = "root",
        description = "Endpoints used for displaying server information."
)
public class RootController {

    String banner;

    BuildProperties buildProperties;

    @Autowired
    public RootController(
            Banner banner,
            Environment environment,
            BuildProperties buildProperties
    ) {
        try (
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream)
        ) {
            banner.printBanner(environment, null, printStream);
            this.banner = outputStream.toString();
            this.buildProperties = buildProperties;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @GetMapping
    @Operation(summary = "Ping the server")
    @ApiResponse(responseCode = "200", description = "OK")
    public ResponseEntity<?> root() {
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/version", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "Display the SEART banner along with the platform build information.")
    @ApiResponse(responseCode = "200", description = "OK")
    public String version() {
        String name = buildProperties.getName();
        String version = buildProperties.getVersion();
        Instant instant = buildProperties.getTime();
        return "<pre style=\"word-wrap: break-word; white-space: pre-wrap;\">" + banner + "</pre>" +
                "<p>" + name + ", version: " + version + ", built on: " + instant + "</p>";
    }
}
