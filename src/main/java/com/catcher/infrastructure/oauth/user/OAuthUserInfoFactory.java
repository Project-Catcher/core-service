package com.catcher.infrastructure.oauth.user;

import com.catcher.core.domain.entity.enums.UserProvider;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OAuthUserInfoFactory {
    public static OAuthUserInfo getOAuthUserInfo(UserProvider userProvider, Map<String, Object> attributes) {
        return switch (userProvider) {
            case KAKAO -> new KakaoOAuthUserInfo(attributes);
            case NAVER -> new NaverOAuthUserInfo(attributes);
            default -> throw new RuntimeException(); // TODO : Custom Exception handling - hg
        };
    }
}
