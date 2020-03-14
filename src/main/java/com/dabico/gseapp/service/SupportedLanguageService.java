package com.dabico.gseapp.service;

import com.dabico.gseapp.model.SupportedLanguage;

public interface SupportedLanguageService {
    SupportedLanguage save(SupportedLanguage sl);
    void delete(SupportedLanguage sl);
}
