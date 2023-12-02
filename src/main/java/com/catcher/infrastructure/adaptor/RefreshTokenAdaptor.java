package com.catcher.infrastructure.adaptor;

import com.catcher.common.exception.BaseException;
import com.catcher.config.JwtTokenProvider;
import com.catcher.core.database.DBManager;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.catcher.common.BaseResponseStatus.NOT_EXIST_REFRESH_JWT;
import static com.catcher.utils.JwtUtils.ACCESS_TOKEN_EXPIRATION_MILLIS;
import static com.catcher.utils.JwtUtils.REFRESH_TOKEN_EXPIRATION_MILLIS;
import static com.catcher.utils.KeyGenerator.AuthType.BLACK_LIST_ACCESS_TOKEN;
import static com.catcher.utils.KeyGenerator.AuthType.REFRESH_TOKEN;
import static com.catcher.utils.KeyGenerator.generateKey;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenAdaptor implements AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final DBManager dbManager;

    @Override
    public TokenDto reissueRefreshToken(String refreshToken) throws BaseException {
        jwtTokenProvider.validateToken(refreshToken);

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);

        String redisRefreshToken = getRefreshToken(generateKey(authentication.getName(), REFRESH_TOKEN));

        compareRefreshToken(refreshToken, redisRefreshToken);

        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

        dbManager.deleteKey(generateKey(refreshToken, REFRESH_TOKEN));
        dbManager.putValue(generateKey(authentication.getName(), REFRESH_TOKEN), newRefreshToken, REFRESH_TOKEN_EXPIRATION_MILLIS);

        return new TokenDto(newAccessToken, newRefreshToken);
    }

    @Override
    public void discardRefreshToken(String refreshToken) {
        try {
            jwtTokenProvider.validateToken(refreshToken);
            Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
            Optional<String> refreshTokenOptional = dbManager.getValue(generateKey(refreshToken, REFRESH_TOKEN));
            if (refreshTokenOptional.isPresent()) {
                compareRefreshToken(refreshToken, refreshTokenOptional.get());
            }
            dbManager.deleteKey(generateKey(authentication.getName(), REFRESH_TOKEN));
        } catch (BaseException e) {
            log.warn("ErrorCode = {}, Message = {}", e.getStatus().getCode(), e.getStatus().getMessage());
        }
    }

    @Override
    public void discardAccessToken(String accessToken) {
        try {
            accessToken = getAccessToken(accessToken);
            jwtTokenProvider.validateToken(accessToken);
            dbManager.putValue(generateKey(accessToken, BLACK_LIST_ACCESS_TOKEN), "", ACCESS_TOKEN_EXPIRATION_MILLIS);
        } catch (BaseException e) {
            log.warn("ErrorCode = {}, Message = {}", e.getStatus().getCode(), e.getStatus().getMessage());
        }
    }

    private String getAccessToken(String accessToken) {
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            return accessToken.substring(7);
        }
        return null;
    }

    private String getRefreshToken(String key) {
        return dbManager.getValue(key)
                .orElseThrow(() -> new BaseException(NOT_EXIST_REFRESH_JWT));
    }

    private void compareRefreshToken(String userRefreshToken, String redisRefreshToken) {
        if (!redisRefreshToken.equals(userRefreshToken)) {
            throw new BaseException(NOT_EXIST_REFRESH_JWT);
        }
    }
}
