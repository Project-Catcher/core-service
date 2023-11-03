package com.catcher.infrastructure.adaptor;

import com.catcher.common.exception.BaseException;
import com.catcher.config.JwtTokenProvider;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.service.ApiService;
import com.catcher.infrastructure.RedisManager;
import com.catcher.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import static com.catcher.common.BaseResponseStatus.NOT_EXIST_REFRESH_JWT;
import static com.catcher.utils.JwtUtils.*;

@Component
@RequiredArgsConstructor
public class RefreshTokenAdaptor implements ApiService<TokenDto> {
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisManager redisManager;

    @Override
    public TokenDto reissueRefreshToken(String refreshToken) throws BaseException {
        jwtTokenProvider.validateToken(refreshToken);

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);

        String redisRefreshToken = getRefreshToken(authentication.getName());

        compareRefreshToken(refreshToken, redisRefreshToken);

        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

        redisManager.deleteKey(refreshToken);
        redisManager.putValue(authentication.getName(), newRefreshToken, REFRESH_TOKEN_EXPIRATION_MILLIS);

        return new TokenDto(newAccessToken, newRefreshToken);
    }

    private String getRefreshToken(String name) {
        return redisManager.getValue(name)
                .orElseThrow(() -> new BaseException(NOT_EXIST_REFRESH_JWT));
    }

    private void compareRefreshToken(String userRefreshToken, String redisRefreshToken) {
        if (!redisRefreshToken.equals(userRefreshToken)) {
            throw new BaseException(NOT_EXIST_REFRESH_JWT);
        }
    }
}
