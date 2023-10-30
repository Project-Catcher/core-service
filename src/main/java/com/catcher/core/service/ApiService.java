package com.catcher.core.service;

import com.catcher.core.dto.TokenDto;

public interface ApiService<T> {
    TokenDto reissueRefreshToken(String refreshToken);
}
