package usi.si.seart.gseapp.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import usi.si.seart.gseapp.db_access_service.AccessTokenService;
import usi.si.seart.gseapp.db_access_service.ApplicationPropertyService;
import usi.si.seart.gseapp.db_access_service.CrawlJobService;
import usi.si.seart.gseapp.db_access_service.GitRepoService;
import usi.si.seart.gseapp.db_access_service.SupportedLanguageService;
import usi.si.seart.gseapp.dto.AccessTokenDto;
import usi.si.seart.gseapp.dto.CrawlJobDto;
import usi.si.seart.gseapp.model.AccessToken;
import usi.si.seart.gseapp.model.CrawlJob;
import usi.si.seart.gseapp.model.SupportedLanguage;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;

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
    ApplicationPropertyService applicationPropertyService;
    GitRepoService gitRepoService;

    @GetMapping("/api/t")
    public ResponseEntity<?> getTokens(){
        List<AccessToken> tokens = accessTokenService.getAll();
        List<AccessTokenDto> dtos = List.of(
                conversionService.convert(tokens.toArray(new AccessToken[0]), AccessTokenDto[].class)
        );

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/api/t")
    public ResponseEntity<?> addToken(@RequestBody String value){
        AccessToken created = accessTokenService.create(AccessToken.builder().value(value).build());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/api/t/{tokenId}")
    public ResponseEntity<?> deleteToken(@PathVariable(value = "tokenId") Long tokenId){
        try {
            accessTokenService.delete(tokenId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex){
            log.error(ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/l")
    public ResponseEntity<?> getLanguages(){
        return ResponseEntity.ok(Map.of("items", supportedLanguageService.getAll()));
    }

    @PostMapping("/api/l")
    public ResponseEntity<?> addLanguage(@RequestBody String language){
        SupportedLanguage created = supportedLanguageService.create(SupportedLanguage.builder().name(language).build());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/api/l/stats")
    public ResponseEntity<?> getLanguageStatistics(){
        return ResponseEntity.ok(gitRepoService.getAllLanguageStatistics());
    }

    /**
     * Return the data to be displayed in "Stat" popup (number of processed repo for each lanugae)
     */
    @GetMapping("/api/r/stats")
    public ResponseEntity<?> getRepoStatistics(){
        return ResponseEntity.ok(gitRepoService.getMainLanguageStatistics());
    }

    @DeleteMapping("/api/l/{langId}")
    public ResponseEntity<?> deleteLanguage(@PathVariable(value = "langId") Long langId){
        try {
            supportedLanguageService.delete(langId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex){
            log.error(ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/j")
    public ResponseEntity<?> getCompletedJobs(){
        List<CrawlJob> jobs = crawlJobService.getCompletedJobs();
        List<CrawlJobDto> dtos = List.of(
                conversionService.convert(jobs.toArray(new CrawlJob[0]), CrawlJobDto[].class)
        );

        return ResponseEntity.ok(Map.of("items", dtos));
    }

    @GetMapping("/api/s")
    public ResponseEntity<?> getSchedulingRate(){
        return ResponseEntity.ok(applicationPropertyService.getCrawlScheduling());
    }

    @PutMapping("/api/s")
    public ResponseEntity<?> setSchedulingRate(@RequestBody Long rate){
        applicationPropertyService.setCrawlScheduling(rate);
        return ResponseEntity.ok().build();
    }
}
