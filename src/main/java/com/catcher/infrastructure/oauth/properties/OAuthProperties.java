package com.catcher.infrastructure.oauth.properties;

import com.catcher.core.domain.entity.enums.UserProvider;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.util.Map;

public interface OAuthProperties {
    boolean support(UserProvider userProvider);
    MultiValueMap<String, String> getSignUpJsonBody(Map params);
    MultiValueMap<String, String> getLoginJsonBody(Map params);
    MultiValueMap<String, String> getLogoutJsonBody(String accessToken);
    URI getUserInfoUri();
    URI getTokenUri();
    URI getLogoutUri();
    UserProvider getProvider();
}
