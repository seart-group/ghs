package com.dabico.gseapp.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class LanguageSizeDtoList {
    @Builder.Default
    List<LanguageSizeDto> items = new ArrayList<>();
}
