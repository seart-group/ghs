package com.dabico.gseapp.service;

import com.dabico.gseapp.model.SupportedLanguage;
import com.dabico.gseapp.repository.SupportedLanguageRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class SupportedLanguageServiceImpl implements SupportedLanguageService {
    SupportedLanguageRepository supportedLanguageRepository;

    @Override
    public SupportedLanguage create(SupportedLanguage sl){
        return supportedLanguageRepository.save(sl);
    }

    @Override
    public void delete(SupportedLanguage sl){
        supportedLanguageRepository.delete(sl);
    }
}
