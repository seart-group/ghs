package usi.si.seart.gseapp.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import usi.si.seart.gseapp.dto.AccessTokenDto;
import usi.si.seart.gseapp.model.AccessToken;

public class AccessTokenToDtoConverter implements Converter<AccessToken, AccessTokenDto> {

    @Override
    @NonNull
    public AccessTokenDto convert(@NonNull AccessToken source) {
        return AccessTokenDto.builder()
                .id(source.getId())
                .value(source.getValue())
                .build();
    }
}
