package usi.si.seart.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Date;

@RestController
public class RootController {
    @RequestMapping("/")
    public String root() { return "Spring Boot is running: " + Date.from(Instant.now()); }
}
