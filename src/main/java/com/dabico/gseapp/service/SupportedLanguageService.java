package com.dabico.gseapp.service;

import com.dabico.gseapp.dto.SupportedLanguageDto;
import com.dabico.gseapp.dto.SupportedLanguageDtoList;
import com.dabico.gseapp.model.SupportedLanguage;

public interface SupportedLanguageService {
    SupportedLanguageDtoList getAll();
    SupportedLanguageDto create(SupportedLanguage language);
    void delete(Long id);
}
