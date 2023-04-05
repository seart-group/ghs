package usi.si.seart.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@AllArgsConstructor(onConstructor_ = @Autowired)
public class RootController {

    private final String banner;

    @RequestMapping("/")
    public String root() {
        return "<pre style=\"word-wrap: break-word; white-space: pre-wrap;\">"+banner+"</pre>" +
                "<p>Spring Boot server is running: "+Instant.now()+"</p>";
    }
}
