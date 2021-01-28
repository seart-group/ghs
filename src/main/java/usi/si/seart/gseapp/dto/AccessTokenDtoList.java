package usi.si.seart.gseapp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class AccessTokenDtoList {
    @Builder.Default
    List<AccessTokenDto> items = new ArrayList<>();
}
