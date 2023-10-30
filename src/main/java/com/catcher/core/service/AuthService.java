package com.catcher.core.service;

import com.catcher.common.exception.BaseException;
import com.catcher.config.JwtTokenProvider;
import com.catcher.core.dto.TokenDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import static com.catcher.common.BaseResponseStatus.NOT_EXIST_REFRESH_JWT;

@Service
@RequiredArgsConstructor
public class AuthService {
    // old Version
    // AuthController -> AuthService(구현)
    // new Version
    // AuthController -> ApiService(interface) -> infra/Adaptor(구현)
//    private final JwtTokenProvider jwtTokenProvider;
//
//    private final RedisTemplate<String, String> redisTemplate;
//
//    public TokenDto reissueToken(String refreshToken) throws BaseException {
//        // Refresh Token 검증
//        jwtTokenProvider.validateToken(refreshToken);
//
//        // Access Token 에서 User Name를 가져옴
//        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
//
//        // Redis에서 저장된 Refresh Token 값을 가져옴
//        String redisRefreshToken = redisTemplate.opsForValue().get(authentication.getName());
//        if(!redisRefreshToken.equals(refreshToken)) {
//            throw new BaseException(NOT_EXIST_REFRESH_JWT);
//        }
//        // 토큰 재발행
//        TokenDto tokenDto = new TokenDto(
//                jwtTokenProvider.createAccessToken(authentication),
//                jwtTokenProvider.createRefreshToken(authentication)
//        );
//
//        return tokenDto;
//    }
}
