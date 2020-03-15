package com.dabico.gseapp.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
public class AccessTokenDtoList {
    List<AccessTokenDto> items;
}
