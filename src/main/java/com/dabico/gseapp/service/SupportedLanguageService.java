package com.dabico.gseapp.service;

import com.dabico.gseapp.dto.StringList;
import com.dabico.gseapp.dto.SupportedLanguageDto;
import com.dabico.gseapp.model.SupportedLanguage;

public interface SupportedLanguageService {
    StringList getAll();
    SupportedLanguageDto create(SupportedLanguage language);
    void delete(Long id);
}
