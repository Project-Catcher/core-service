package com.catcher.infrastructure.oauth.properties;

import com.catcher.core.domain.entity.enums.UserProvider;
import com.catcher.infrastructure.KmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NaverProperties implements OAuthProperties{
    private final KmsService kmsService;
    @Value("${oauth2.client.registration.naver.client-id}")
    private String clientId;
    @Value("${oauth2.client.registration.naver.client-secret}")
    private String clientSecret;
    @Value("${oauth2.client.registration.naver.redirect-uri.signup}")
    private String signUpUri;
    @Value("${oauth2.client.registration.naver.redirect-uri.login}")
    private String loginUri;
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
    public MultiValueMap<String, String> getSignUpJsonBody(Map params) {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("code", params.get("code").toString());
        multiValueMap.add("grant_type", grantType);
        multiValueMap.add("redirect_uri", signUpUri);
        multiValueMap.add("client_secret", kmsService.decrypt(clientSecret));
        multiValueMap.add("client_id", kmsService.decrypt(clientId));
        multiValueMap.add("state", params.get("state").toString());

        return multiValueMap;
    }

    @Override
    public MultiValueMap<String, String> getLoginJsonBody(Map params) {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("code", params.get("code").toString());
        multiValueMap.add("grant_type", grantType);
        multiValueMap.add("redirect_uri", loginUri);
        multiValueMap.add("client_secret", kmsService.decrypt(clientSecret));
        multiValueMap.add("client_id", kmsService.decrypt(clientId));
        multiValueMap.add("state", params.get("state").toString());

        return multiValueMap;
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
    public UserProvider getProvider() {
        return UserProvider.NAVER;
    }
}
