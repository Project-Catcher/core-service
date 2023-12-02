package com.catcher.infrastructure.oauth.properties;

import com.catcher.core.domain.entity.enums.UserProvider;
import com.catcher.infrastructure.utils.KmsUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KakaoProperties implements OAuthProperties{
    private final KmsUtils kmsUtils;

    @Value("${oauth2.client.registration.kakao.client-id}")
    private String clientId;
    @Value("${oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;
    @Value("${oauth2.client.registration.kakao.redirect-uri.signup}")
    private String signupUri;
    @Value("${oauth2.client.registration.kakao.redirect-uri.login}")
    private String loginUri;
    @Value("${oauth2.client.registration.kakao.redirect-uri.logout}")
    private String logoutUri;
    @Value("${oauth2.client.registration.kakao.authorization-grant-type}")
    private String grantType;
    @Value("${oauth2.client.provider.kakao.token-uri}")
    private String tokenUri;
    @Value("${oauth2.client.provider.kakao.user-info-uri}")
    private String userInfoUri;

    @Override
    public MultiValueMap<String, String> getSignUpJsonBody(Map params) {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("code", params.get("code").toString());
        multiValueMap.add("grant_type", grantType);
        multiValueMap.add("redirect_uri", signupUri);
        multiValueMap.add("client_secret", kmsUtils.decrypt(clientSecret));
        multiValueMap.add("client_id", kmsUtils.decrypt(clientId));

        return multiValueMap;
    }

    @Override
    public MultiValueMap<String, String> getLoginJsonBody(Map params) {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("code", params.get("code").toString());
        multiValueMap.add("grant_type", grantType);
        multiValueMap.add("redirect_uri", loginUri);
        multiValueMap.add("client_secret", kmsUtils.decrypt(clientSecret));
        multiValueMap.add("client_id", kmsUtils.decrypt(clientId));

        return multiValueMap;
    }

    @Override
    public MultiValueMap<String, String> getLogoutJsonBody(String accessToken) {
        return null;
    }

    @Override
    public URI getUserInfoUri() {
        return URI.create(userInfoUri);
    }

    @Override
    public URI getTokenUri() {
        return URI.create(tokenUri);
    }

    @Override
    public URI getLogoutUri() {
        return URI.create(logoutUri);
    }

    @Override
    public UserProvider getProvider() {
        return UserProvider.KAKAO;
    }
}
