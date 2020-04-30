package com.dabico.gseapp.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class AccessTokenDtoList {
    @Builder.Default
    List<AccessTokenDto> items = new ArrayList<>();
}
