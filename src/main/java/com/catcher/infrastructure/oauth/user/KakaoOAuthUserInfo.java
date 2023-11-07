package com.catcher.infrastructure.oauth.user;

import com.catcher.core.domain.entity.enums.UserProvider;

import java.util.Map;

public class KakaoOAuthUserInfo extends OAuthUserInfo {
    protected KakaoOAuthUserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return this.getProvider().name() + "_" + attributes.get("id");
    }

    @Override
    public String getEmail() {
        return (((Map<String, Object>) attributes.get("kakao_account")).get("email")).toString();
    }

    @Override
    public UserProvider getProvider() {
        return UserProvider.KAKAO;
    }
}
