package com.catcher.infrastructure.oauth.properties;

import com.catcher.core.domain.entity.enums.UserProvider;
import org.springframework.util.MultiValueMap;

import java.util.Map;

public interface OAuthProperties {
    boolean support(UserProvider userProvider);
    MultiValueMap<String, String> getJsonBody(Map params);
    String getUserInfoUri();
    String getTokenUri();
    UserProvider getProvider();
}
