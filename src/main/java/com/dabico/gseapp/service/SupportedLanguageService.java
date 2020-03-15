package com.dabico.gseapp.service;

import com.dabico.gseapp.dto.SupportedLanguageDto;
import com.dabico.gseapp.dto.SupportedLanguageDtoList;

public interface SupportedLanguageService {
    SupportedLanguageDtoList getAll();
    void createOrUpdate(SupportedLanguageDto dto);
    void delete(Long id);
}
