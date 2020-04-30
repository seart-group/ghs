package com.dabico.gseapp.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class StringLongDtoList {
    @Builder.Default
    List<StringLongDto> items = new ArrayList<>();
}
