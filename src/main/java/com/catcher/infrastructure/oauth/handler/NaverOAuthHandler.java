package com.catcher.infrastructure.oauth.handler;

import com.catcher.infrastructure.oauth.properties.OAuthProperties;
import com.catcher.resource.external.OAuthFeignController;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.net.URI;

@Component
public class NaverOAuthHandler extends OAuthHandler {
    public NaverOAuthHandler(OAuthFeignController oAuthFeignController,
                             @Qualifier("naverProperties") OAuthProperties oAuthProperties) {
        super(oAuthFeignController, oAuthProperties);
    }

    @Override
    public void invalidateToken(String accessToken) {
        URI logoutUri = getOAuthProperties().getLogoutUri();
        MultiValueMap<String, String> logoutJsonBody = getOAuthProperties().getLogoutJsonBody(accessToken);

        OAuthFeignController oAuthFeignController = getOAuthFeignController();
        oAuthFeignController.postWithBody(logoutUri, logoutJsonBody);
    }
}
