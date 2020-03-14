package com.dabico.gseapp.service;

import com.dabico.gseapp.dto.SupportedLanguageDto;
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
    public void createOrUpdate(SupportedLanguageDto sldto){
        SupportedLanguage sl = SupportedLanguage.builder().build();
        if (sldto.getId() != null){
            sl = supportedLanguageRepository.findById(sldto.getId()).get();
        }
        sl.setLanguage(sldto.getLanguage());
        supportedLanguageRepository.save(sl);
    }

    @Override
    public void delete(Long id){ supportedLanguageRepository.deleteById(id); }
}
