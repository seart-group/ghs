package com.dabico.gseapp.service;

import com.dabico.gseapp.dto.SupportedLanguageDtoList;
import com.dabico.gseapp.model.SupportedLanguage;

public interface SupportedLanguageService {
    SupportedLanguageDtoList getAll();
    void create(SupportedLanguage language);
    void delete(Long id);
}
