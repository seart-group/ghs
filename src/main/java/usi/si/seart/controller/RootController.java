package usi.si.seart.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "root", description = "Endpoints used primarily API status for checks.")
public class RootController {

    String banner;

    BuildProperties buildProperties;

    @Autowired
    @SneakyThrows(IOException.class)
    public RootController(
            @Value("${spring.banner.location}") Resource resource,
            BuildProperties buildProperties
    ) {
        InputStream inputStream = resource.getInputStream();
        this.banner = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        this.buildProperties = buildProperties;
    }

    @GetMapping
    public ResponseEntity<?> root() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/version")
    public String version() {
        String name = buildProperties.getName();
        String version = buildProperties.getVersion();
        Instant instant = buildProperties.getTime();
        return "<pre style=\"word-wrap: break-word; white-space: pre-wrap;\">"+banner+"</pre>" +
                "<p>"+name+", version: "+version+", built on: "+instant+"</p>";
    }
}
