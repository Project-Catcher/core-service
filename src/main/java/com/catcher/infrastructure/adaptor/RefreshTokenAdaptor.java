package com.catcher.infrastructure.adaptor;

import com.catcher.common.exception.BaseException;
import com.catcher.config.JwtTokenProvider;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.service.ApiService;
import com.catcher.infrastructure.RedisManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import static com.catcher.common.BaseResponseStatus.NOT_EXIST_REFRESH_JWT;

@Component
@RequiredArgsConstructor
public class RefreshTokenAdaptor implements ApiService<TokenDto> {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisManager redisManager;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public TokenDto reissueRefreshToken(String refreshToken) throws BaseException {
        jwtTokenProvider.validateToken(refreshToken);

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);

        String redisRefreshToken = getRefreshToken(authentication.getName());

        compareRefreshToken(refreshToken, redisRefreshToken);

        TokenDto tokenDto = new TokenDto(
                jwtTokenProvider.createAccessToken(authentication),
                jwtTokenProvider.createRefreshToken(authentication)
        );

        return tokenDto;
    }

    private String getRefreshToken(String name) {
        return redisManager.getValue(name)
                .orElseThrow(() -> new BaseException(NOT_EXIST_REFRESH_JWT));
    }

    private void compareRefreshToken(String userRefreshToken, String redisRefreshToken) {
        if(!redisRefreshToken.equals(userRefreshToken)) {
            throw new BaseException(NOT_EXIST_REFRESH_JWT);
        }
    }
}
