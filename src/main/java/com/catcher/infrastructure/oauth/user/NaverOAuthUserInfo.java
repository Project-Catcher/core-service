package com.catcher.infrastructure.oauth.user;


import com.catcher.core.domain.entity.enums.UserProvider;

import java.util.Map;

public class NaverOAuthUserInfo extends OAuthUserInfo{
    protected NaverOAuthUserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return String.valueOf(((Map<String, Object>) attributes.get("response")).get("id"));
    }

    @Override
    public String getEmail() {
        return String.valueOf(((Map<String, Object>) attributes.get("response")).get("email"));
    }

    @Override
    public UserProvider getProvider() {
        return UserProvider.NAVER;
    }
}
