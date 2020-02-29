package com.dabico;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class GSEApplication {
	public static void main(String[] args) {
		SpringApplication.run(GSEApplication.class, args);
	}
}