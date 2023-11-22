package com.catcher.core.service;

import com.catcher.core.dto.TokenDto;

public interface AuthService {
    TokenDto reissueRefreshToken(String refreshToken);

    void discardRefreshToken(String refreshToken);
}
