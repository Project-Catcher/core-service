package com.catcher.infrastructure.oauth.properties;

import com.catcher.common.exception.BaseException;
import com.catcher.core.domain.entity.enums.UserProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.catcher.common.BaseResponseStatus.*;

@Component
@RequiredArgsConstructor
public class OAuthPropertiesHandler {
    private List<OAuthProperties> oAuthProperties;
    private final ApplicationContext applicationContext;

    @PostConstruct
    void postConstruct() {
        this.oAuthProperties = new ArrayList<>();
        String[] names = applicationContext.getBeanNamesForType(OAuthProperties.class);
        for (String name : names) {
            OAuthProperties bean = (OAuthProperties) applicationContext.getBean(name);
            this.oAuthProperties.add(bean);
        }
    }

    public OAuthProperties getOAuthProperties(UserProvider userProvider) {
        return oAuthProperties.stream()
                .filter(property -> property.support(userProvider))
                .findAny()
                .orElseThrow(() -> new BaseException(INVALID_USER_OAUTH_TYPE));
    }
}
