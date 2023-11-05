package com.catcher.infrastructure.oauth.handler;

import com.catcher.infrastructure.oauth.properties.OAuthProperties;
import com.catcher.resource.external.OAuthFeignController;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class KakaoOAuthHandler extends OAuthHandler {
    public KakaoOAuthHandler(OAuthFeignController oAuthFeignController,
                             @Qualifier("kakaoProperties") OAuthProperties oAuthProperties) {
        super(oAuthFeignController, oAuthProperties);
    }

    @Override
    public void invalidateToken(String accessToken) {
        URI logoutUri = getOAuthProperties().getLogoutUri();
        OAuthFeignController oAuthFeignController = getOAuthFeignController();
        oAuthFeignController.postWithAuthorizationHeader(logoutUri, "Bearer " + accessToken);
    }
}
