package com.dabico.gseapp.service;

import com.dabico.gseapp.repository.AccessTokenRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AccessTokenServiceImpl implements AccessTokenService {
    AccessTokenRepository accessTokenRepository;
}
