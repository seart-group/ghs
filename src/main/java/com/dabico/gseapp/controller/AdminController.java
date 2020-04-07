package com.dabico.gseapp.controller;

import com.dabico.gseapp.converter.AccessTokenConverter;
import com.dabico.gseapp.converter.SupportedLanguageConverter;
import com.dabico.gseapp.dto.AccessTokenDto;
import com.dabico.gseapp.dto.SupportedLanguageDto;
import com.dabico.gseapp.service.AccessTokenService;
import com.dabico.gseapp.service.ApplicationPropertyService;
import com.dabico.gseapp.service.CrawlJobService;
import com.dabico.gseapp.service.SupportedLanguageService;
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
    static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    AccessTokenService accessTokenService;
    AccessTokenConverter accessTokenConverter;
    SupportedLanguageService supportedLanguageService;
    SupportedLanguageConverter supportedLanguageConverter;
    CrawlJobService crawlJobService;
    ApplicationPropertyService applicationPropertyService;

    @GetMapping("/t")
    public ResponseEntity<?> getTokens(){
        return ResponseEntity.ok(accessTokenService.getAll());
    }

    @PostMapping("/t")
    public ResponseEntity<?> addToken(@RequestBody AccessTokenDto token){
        AccessTokenDto created = accessTokenService.create(accessTokenConverter.fromTokenDtoToToken(token));
        if (created != null){
            return new ResponseEntity<>(created,HttpStatus.CREATED);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @DeleteMapping("/t/{tokenId}")
    public ResponseEntity<?> deleteToken(@PathVariable(value = "tokenId") Long tokenId){
        try {
            accessTokenService.delete(tokenId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex){
            logger.error(ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/l")
    public ResponseEntity<?> getLanguages(){
        return ResponseEntity.ok(supportedLanguageService.getAll());
    }

    @PostMapping("/l")
    public ResponseEntity<?> addLanguage(@RequestBody SupportedLanguageDto langDto){
        SupportedLanguageDto created = supportedLanguageService.create(supportedLanguageConverter.fromLanguageDtoToLanguage(langDto));
        if (created != null){
            return new ResponseEntity<>(created,HttpStatus.CREATED);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @DeleteMapping("/l/{langId}")
    public ResponseEntity<?> deleteLanguage(@PathVariable(value = "langId") Long langId){
        try {
            supportedLanguageService.delete(langId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex){
            logger.error(ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/j")
    public ResponseEntity<?> getCompletedJobs(){
        return ResponseEntity.ok(crawlJobService.getCompletedJobs());
    }
}
