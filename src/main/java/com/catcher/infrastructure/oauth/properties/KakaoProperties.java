package com.catcher.infrastructure.oauth.properties;

import com.catcher.core.domain.entity.enums.UserProvider;
import com.catcher.infrastructure.KmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KakaoProperties implements OAuthProperties{
    private final KmsService kmsService;


    @Value("${oauth2.client.registration.kakao.client-id}")
    private String clientId;
    @Value("${oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;
    @Value("${oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;
    @Value("${oauth2.client.registration.kakao.authorization-grant-type}")
    private String grantType;
    @Value("${oauth2.client.provider.kakao.token-uri}")
    private String tokenUri;
    @Value("${oauth2.client.provider.kakao.user-info-uri}")
    private String userInfoUri;

    @Override
    public boolean support(UserProvider userProvider) {
        return userProvider.equals(UserProvider.KAKAO);
    }

    @Override
    public MultiValueMap<String, String> getJsonBody(Map params) {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("code", params.get("code").toString());
        multiValueMap.add("grant_type", grantType);
        multiValueMap.add("redirect_uri", redirectUri);
        multiValueMap.add("client_secret", kmsService.decrypt(clientSecret));
        multiValueMap.add("client_id", kmsService.decrypt(clientId));

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
        return UserProvider.KAKAO;
    }
}
