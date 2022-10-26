package usi.si.seart.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import usi.si.seart.model.SupportedLanguage;
import usi.si.seart.service.GitRepoService;
import usi.si.seart.service.SupportedLanguageService;

import java.util.stream.Collectors;

@Slf4j
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AdminController {

    SupportedLanguageService supportedLanguageService;
    GitRepoService gitRepoService;

    @GetMapping("/l")
    public ResponseEntity<?> getLanguages(){
        return ResponseEntity.ok(
                supportedLanguageService.getAll()
                        .stream()
                        .map(SupportedLanguage::getName)
                        .sorted()
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/l/stats")
    public ResponseEntity<?> getLanguageStatistics(){
        return ResponseEntity.ok(gitRepoService.getAllLanguageStatistics());
    }

    @GetMapping("/r/stats")
    public ResponseEntity<?> getRepoStatistics(){
        return ResponseEntity.ok(gitRepoService.getMainLanguageStatistics());
    }
}
