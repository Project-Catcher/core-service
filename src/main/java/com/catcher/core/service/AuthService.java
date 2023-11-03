package com.catcher.core.service;


public interface AuthService<T> {
    T reissueRefreshToken(String refreshToken);
}
