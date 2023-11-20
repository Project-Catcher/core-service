package com.catcher.infrastructure.oauth.handler;

import com.catcher.common.exception.BaseException;
import com.catcher.core.domain.entity.enums.UserProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.catcher.common.BaseResponseStatus.INVALID_USER_OAUTH_TYPE;

@Component
@RequiredArgsConstructor
public class OAuthHandlerFactory {
    private final ApplicationContext applicationContext;
    private List<OAuthHandler> oAuthHandlers;

    @PostConstruct
    void initializeOAuthHandlers() {
        oAuthHandlers = new ArrayList<>();
        String[] names = applicationContext.getBeanNamesForType(OAuthHandler.class);

        for (String name : names) {
            OAuthHandler bean = (OAuthHandler) applicationContext.getBean(name);
            oAuthHandlers.add(bean);
        }
    }

    public OAuthHandler getOAuthHandler(UserProvider userProvider) {
        return oAuthHandlers
                .stream()
                .filter(handler -> handler.support(userProvider))
                .findAny()
                .orElseThrow(
                        () -> new BaseException(INVALID_USER_OAUTH_TYPE)
                );
    }
}
