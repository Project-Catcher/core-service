package com.catcher.infrastructure.oauth.handler;

import com.catcher.common.exception.OAuthException;
import com.catcher.core.domain.entity.enums.UserProvider;
import com.catcher.infrastructure.oauth.OAuthTokenResponse;
import com.catcher.infrastructure.oauth.properties.OAuthProperties;
import com.catcher.infrastructure.oauth.user.OAuthUserInfo;
import com.catcher.infrastructure.oauth.user.OAuthUserInfoFactory;
import com.catcher.resource.external.OAuthFeignController;
import feign.FeignException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Supplier;

import static com.catcher.common.BaseResponseStatus.OAUTH_GENERATE_TOKEN_ERROR;

@Slf4j
@RequiredArgsConstructor
@Getter
public abstract class RefactorOAuthHandler {
    private final OAuthFeignController oAuthFeignController;
    private final OAuthProperties oAuthProperties;

    public OAuthTokenResponse handleToken(Supplier<Map> supplier) {
        String accessToken = null;
        try {
            OAuthTokenResponse oAuthTokenResponse = oAuthFeignController.getWithRequestParams(oAuthProperties.getTokenUri(), supplier.get());
            accessToken = oAuthTokenResponse.getAccessToken();

            return oAuthTokenResponse;
        } catch (FeignException e) {
            log.error("OAUTH-{} : description = {}",oAuthProperties.getProvider().name(), e.getMessage());
            throw new OAuthException(OAUTH_GENERATE_TOKEN_ERROR, accessToken);
        }
    }

    public OAuthUserInfo handleUserInfo(String accessToken) {
        log.info("access token = {}", accessToken);
        try {
            Map map = oAuthFeignController.postWithAuthorizationHeader(oAuthProperties.getUserInfoUri(), "Bearer " + accessToken);

            return OAuthUserInfoFactory.getOAuthUserInfo(oAuthProperties.getProvider(), map);
        } catch (FeignException e) {
            log.error("OAUTH-{} : description = {}", oAuthProperties.getProvider().name(), e.getMessage());
            throw new OAuthException(OAUTH_GENERATE_TOKEN_ERROR, accessToken);
        }
    }

    public boolean support(UserProvider userProvider) {
        return this.getOAuthProperties().getProvider().equals(userProvider);
    }

    public abstract void invalidateToken(String accessToken);
}
