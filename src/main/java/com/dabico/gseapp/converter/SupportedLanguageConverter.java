package com.dabico.gseapp.converter;

import com.dabico.gseapp.dto.SupportedLanguageDto;
import com.dabico.gseapp.model.SupportedLanguage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SupportedLanguageConverter {
    public List<SupportedLanguageDto> fromLanguagesToLanguagesDto(List<SupportedLanguage> languages){
        return languages.stream().map(this::fromLanguageToLanguageDto).collect(Collectors.toList());
    }

    public SupportedLanguageDto fromLanguageToLanguageDto(SupportedLanguage lang){
        return SupportedLanguageDto.builder().id(lang.getId()).language(lang.getName()).build();
    }

    public SupportedLanguage fromLanguageDtoToLanguage(SupportedLanguageDto dto){
        return SupportedLanguage.builder().id(dto.getId()).name(dto.getLanguage()).build();
    }
}
