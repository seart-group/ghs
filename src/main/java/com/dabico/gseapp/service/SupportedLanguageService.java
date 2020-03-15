package com.dabico.gseapp.service;

import com.dabico.gseapp.dto.SupportedLanguageDto;
import com.dabico.gseapp.dto.SupportedLanguageDtoList;

public interface SupportedLanguageService {
    SupportedLanguageDtoList getAll();
    void createOrUpdate(SupportedLanguageDto sldto);
    void delete(Long id);
}
