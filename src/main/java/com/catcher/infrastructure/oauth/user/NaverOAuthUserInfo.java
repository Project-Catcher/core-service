package com.catcher.infrastructure.oauth.user;


import com.catcher.core.domain.entity.enums.UserProvider;

import java.util.Map;

public class NaverOAuthUserInfo extends OAuthUserInfo {
    protected NaverOAuthUserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return this.getProvider().name() + "_" + ((Map<String, Object>) attributes.get("response")).get("id");
    }

    @Override
    public String getEmail() {
        return (((Map<String, Object>) attributes.get("response")).get("email")).toString();
    }

    @Override
    public UserProvider getProvider() {
        return UserProvider.NAVER;
    }
}
