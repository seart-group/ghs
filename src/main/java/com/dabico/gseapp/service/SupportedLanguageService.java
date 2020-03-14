package com.dabico.gseapp.service;

import com.dabico.gseapp.dto.SupportedLanguageDto;

public interface SupportedLanguageService {
    void createOrUpdate(SupportedLanguageDto sldto);
    void delete(Long id);
}
