package com.catcher.infrastructure.oauth.properties;

import com.catcher.core.domain.entity.enums.UserProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Component
public class NaverProperties implements OAuthProperties{
    @Value("${oauth2.client.registration.naver.client-id}")
    private String clientId;
    @Value("${oauth2.client.registration.naver.client-secret}")
    private String clientSecret;
    @Value("${oauth2.client.registration.naver.redirect-uri}")
    private String redirectUri;
    @Value("${oauth2.client.registration.naver.authorization-grant-type}")
    private String grantType;
    @Value("${oauth2.client.provider.naver.token_uri}")
    private String tokenUri;
    @Value("${oauth2.client.provider.naver.user-info-uri}")
    private String userInfoUri;

    @Override
    public boolean support(UserProvider userProvider) {
        return userProvider.equals(UserProvider.NAVER);
    }

    @Override
    public MultiValueMap<String, String> getJsonBody(Map params) {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("code", params.get("code").toString());
        multiValueMap.add("grant_type", grantType);
        multiValueMap.add("redirect_uri", redirectUri);
        multiValueMap.add("client_secret", clientSecret);
        multiValueMap.add("client_id", clientId);
        multiValueMap.add("state", params.get("state").toString());

        return multiValueMap;
    }

    @Override
    public String getUserInfoUri() {
        return userInfoUri;
    }

    @Override
    public String getTokenUri() {
        return tokenUri;
    }

    @Override
    public UserProvider getProvider() {
        return UserProvider.NAVER;
    }
}
