package usi.si.seart.gseapp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AccessTokenDto {
    Long id;
    String value;
}
