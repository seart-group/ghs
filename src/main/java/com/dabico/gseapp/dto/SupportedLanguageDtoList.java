package com.dabico.gseapp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class SupportedLanguageDtoList {
    @Builder.Default
    List<SupportedLanguageDto> items = new ArrayList<>();
}
