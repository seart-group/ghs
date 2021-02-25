package usi.si.seart.gseapp.controller;

import usi.si.seart.gseapp.converter.AccessTokenConverter;
import usi.si.seart.gseapp.converter.SupportedLanguageConverter;
import usi.si.seart.gseapp.db_access_service.*;
import usi.si.seart.gseapp.dto.AccessTokenDto;
import usi.si.seart.gseapp.dto.SupportedLanguageDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AdminController {
    static Logger logger = LoggerFactory.getLogger(AdminController.class);

    AccessTokenService accessTokenService;
    AccessTokenConverter accessTokenConverter;
    SupportedLanguageService supportedLanguageService;
    SupportedLanguageConverter supportedLanguageConverter;
    CrawlJobService crawlJobService;
    ApplicationPropertyService applicationPropertyService;
    GitRepoService gitRepoService;

    @GetMapping("/api/t")
    public ResponseEntity<?> getTokens(){
        return ResponseEntity.ok(accessTokenService.getAll());
    }

    @PostMapping("/api/t")
    public ResponseEntity<?> addToken(@RequestBody AccessTokenDto token){
        AccessTokenDto created = accessTokenService.create(accessTokenConverter.fromTokenDtoToToken(token));
        if (created != null){
            return new ResponseEntity<>(created,HttpStatus.CREATED);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @DeleteMapping("/api/t/{tokenId}")
    public ResponseEntity<?> deleteToken(@PathVariable(value = "tokenId") Long tokenId){
        try {
            accessTokenService.delete(tokenId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex){
            logger.error(ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/l")
    public ResponseEntity<?> getLanguages(){
        return ResponseEntity.ok(supportedLanguageService.getAll());
    }

    @PostMapping("/api/l")
    public ResponseEntity<?> addLanguage(@RequestBody SupportedLanguageDto langDto){
        SupportedLanguageDto created = supportedLanguageService.create(supportedLanguageConverter.fromLanguageDtoToLanguage(langDto));
        if (created != null){
            return new ResponseEntity<>(created,HttpStatus.CREATED);
        } else {
            return ResponseEntity.noContent().build();
        }
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
            logger.error(ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/j")
    public ResponseEntity<?> getCompletedJobs(){
        return ResponseEntity.ok(crawlJobService.getCompletedJobs());
    }

    @GetMapping("/api/s")
    public ResponseEntity<?> getSchedulingRate(){
        return ResponseEntity.ok(applicationPropertyService.getScheduling());
    }

    @PutMapping("/api/s")
    public ResponseEntity<?> setSchedulingRate(@RequestBody Long rate){
        applicationPropertyService.setScheduling(rate);
        return ResponseEntity.ok().build();
    }
}
