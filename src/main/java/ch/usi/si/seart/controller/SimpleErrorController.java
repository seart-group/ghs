package ch.usi.si.seart.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("${server.error.path:${error.path:/error}}")
public class SimpleErrorController implements ErrorController {

    @Hidden
    @RequestMapping
    public ResponseEntity<Void> error(HttpServletResponse response) {
        int code = response.getStatus();
        return code == HttpServletResponse.SC_OK
                ? ResponseEntity.notFound().build()
                : ResponseEntity.status(code).build();
    }
}
