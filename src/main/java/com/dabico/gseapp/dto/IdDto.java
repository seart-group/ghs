package com.dabico.gseapp.dto;

import lombok.*;

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
