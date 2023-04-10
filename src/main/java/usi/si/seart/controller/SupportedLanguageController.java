package usi.si.seart.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import usi.si.seart.model.SupportedLanguage;
import usi.si.seart.service.GitRepoService;
import usi.si.seart.service.SupportedLanguageService;

import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/l")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "supported-language", description = "Endpoints used for retrieving information regarding platform-supported languages.")
public class SupportedLanguageController {

    SupportedLanguageService supportedLanguageService;
    GitRepoService gitRepoService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getLanguages(){
        return ResponseEntity.ok(
                supportedLanguageService.getAll()
                        .stream()
                        .map(SupportedLanguage::getName)
                        .sorted()
                        .collect(Collectors.toList())
        );
    }

    @GetMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getLanguageStatistics(){
        return ResponseEntity.ok(gitRepoService.getAllLanguageStatistics());
    }
}
