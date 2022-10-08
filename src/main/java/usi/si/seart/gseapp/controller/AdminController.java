package usi.si.seart.gseapp.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import usi.si.seart.gseapp.service.AccessTokenService;
import usi.si.seart.gseapp.service.CrawlJobService;
import usi.si.seart.gseapp.service.GitRepoService;
import usi.si.seart.gseapp.service.SupportedLanguageService;
import usi.si.seart.gseapp.dto.AccessTokenDto;
import usi.si.seart.gseapp.dto.CrawlJobDto;
import usi.si.seart.gseapp.model.AccessToken;
import usi.si.seart.gseapp.model.CrawlJob;
import usi.si.seart.gseapp.model.SupportedLanguage;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantConditions")
@Slf4j
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AdminController {

    ConversionService conversionService;
    AccessTokenService accessTokenService;
    SupportedLanguageService supportedLanguageService;
    CrawlJobService crawlJobService;
    GitRepoService gitRepoService;

    @GetMapping("/t")
    public ResponseEntity<?> getTokens(){
        List<AccessToken> tokens = accessTokenService.getAll();
        List<AccessTokenDto> dtos = List.of(
                conversionService.convert(tokens.toArray(new AccessToken[0]), AccessTokenDto[].class)
        );

        return ResponseEntity.ok(dtos);
    }

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

    @GetMapping("/j")
    public ResponseEntity<?> getCompletedJobs(){
        List<CrawlJob> jobs = crawlJobService.getCompletedJobs();
        List<CrawlJobDto> dtos = List.of(conversionService.convert(jobs.toArray(new CrawlJob[0]), CrawlJobDto[].class));
        return ResponseEntity.ok(dtos);
    }
}
