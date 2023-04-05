package usi.si.seart.controller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@AllArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RootController {

    String banner;

    BuildProperties buildProperties;

    @RequestMapping("/")
    public String root() {
        String name = buildProperties.getName();
        String version = buildProperties.getVersion();
        Instant instant = buildProperties.getTime();
        return "<pre style=\"word-wrap: break-word; white-space: pre-wrap;\">"+banner+"</pre>" +
                "<p>"+name+", version: "+version+", built on:"+instant+"</p>";
    }
}
