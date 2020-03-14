package com.dabico.gseapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class IdDto {
    private Long id;

    public static IdDto of(Long id) {
        return IdDto.builder().id(id).build();
    }
}
