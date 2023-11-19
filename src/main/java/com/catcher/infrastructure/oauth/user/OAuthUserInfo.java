package com.catcher.infrastructure.oauth.user;

import com.catcher.core.domain.entity.enums.UserProvider;

import java.util.Map;

public abstract class OAuthUserInfo {
    protected Map<String, Object> attributes;

    public OAuthUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public abstract String getId();
    public abstract String getEmail();
    public abstract UserProvider getProvider();
}
