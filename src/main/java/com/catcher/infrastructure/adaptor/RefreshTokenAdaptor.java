package com.catcher.infrastructure.adaptor;

import com.catcher.common.exception.BaseException;
import com.catcher.config.JwtTokenProvider;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.service.ApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import static com.catcher.common.BaseResponseStatus.NOT_EXIST_REFRESH_JWT;

@Component
@RequiredArgsConstructor
public class RefreshTokenAdaptor implements ApiService<TokenDto> {

    private final JwtTokenProvider jwtTokenProvider;

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public TokenDto reissueRefreshToken(String refreshToken) throws BaseException {
        // Refresh Token 검증
        jwtTokenProvider.validateToken(refreshToken);

        // Access Token 에서 User Name를 가져옴
        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);

        // Redis에서 저장된 Refresh Token 값을 가져옴
        String redisRefreshToken = redisTemplate.opsForValue().get(authentication.getName());
        if(!redisRefreshToken.equals(refreshToken)) {
            throw new BaseException(NOT_EXIST_REFRESH_JWT);
        }
        // 토큰 재발행
        TokenDto tokenDto = new TokenDto(
                jwtTokenProvider.createAccessToken(authentication),
                jwtTokenProvider.createRefreshToken(authentication)
        );

        return tokenDto;
    }
}