package com.dabico.gseapp.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
public class LanguageSizeDtoList {
    List<LanguageSizeDto> items;
}
