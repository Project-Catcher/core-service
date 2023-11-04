package com.catcher.infrastructure.oauth;

import com.catcher.common.exception.BaseException;
import com.catcher.infrastructure.oauth.properties.OAuthProperties;
import com.catcher.infrastructure.oauth.user.OAuthUserInfo;
import com.catcher.infrastructure.oauth.user.OAuthUserInfoFactory;
import com.catcher.resource.external.OAuthFeignController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

import static com.catcher.common.BaseResponseStatus.OAUTH_GENERATE_TOKEN_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthHandler {
    private final OAuthFeignController oAuthFeignController;

    public OAuthTokenResponse getSignUpToken(OAuthProperties oAuthProperties, Map map) {
        try {
            return oAuthFeignController.getWithParams(oAuthProperties.getTokenUri(), oAuthProperties.getSignUpJsonBody(map));
        } catch (HttpClientErrorException e) {
            OAuthTokenResponse oAuthTokenResponse = e.getResponseBodyAs(OAuthTokenResponse.class);
            log.error("OAUTH-{} : error = {}, description = {}",
                    oAuthProperties.getProvider().name(),
                    oAuthTokenResponse.getError(),
                    oAuthTokenResponse.getErrorDescription()
            );
            throw new BaseException(OAUTH_GENERATE_TOKEN_ERROR);
        }
    }

    public OAuthTokenResponse getLoginToken(OAuthProperties oAuthProperties, Map map) {
        try {
            return oAuthFeignController.getWithParams(oAuthProperties.getTokenUri(), oAuthProperties.getLoginJsonBody(map));
        } catch (HttpClientErrorException e) {
            OAuthTokenResponse oAuthTokenResponse = e.getResponseBodyAs(OAuthTokenResponse.class);
            log.error("OAUTH-{} : error = {}, description = {}",
                    oAuthProperties.getProvider().name(),
                    oAuthTokenResponse.getError(),
                    oAuthTokenResponse.getErrorDescription()
            );
            throw new BaseException(OAUTH_GENERATE_TOKEN_ERROR);
        }
    }

    public OAuthUserInfo getOAuthUserInfo(OAuthProperties oAuthProperties, String accessToken) {
        Map map = oAuthFeignController.postWithParams(oAuthProperties.getUserInfoUri(), "Bearer " + accessToken);
        return OAuthUserInfoFactory.getOAuthUserInfo(oAuthProperties.getProvider(), map);
    }
}
