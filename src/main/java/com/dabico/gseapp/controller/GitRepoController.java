package com.dabico.gseapp.controller;

import com.dabico.gseapp.service.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GitRepoController {
    GitRepoService gitRepoService;
    GitRepoLabelService gitRepoLabelService;
    GitRepoLanguageService gitRepoLanguageService;
    AccessTokenService accessTokenService;
    SupportedLanguageService supportedLanguageService;
}
