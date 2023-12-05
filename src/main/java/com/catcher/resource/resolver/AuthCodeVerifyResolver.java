package com.catcher.resource.resolver;

import com.catcher.resource.resolver.annotation.AuthCodeVerifyInject;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static com.catcher.resource.UserController.FIND_ID_URL;
import static com.catcher.resource.UserController.FIND_PW_URL;
import static com.catcher.resource.request.AuthCodeVerifyRequest.IDAuthCodeVerifyRequest;
import static com.catcher.resource.request.AuthCodeVerifyRequest.PWAuthCodeVerifyRequest;
import static com.catcher.utils.HttpServletUtils.getBodyData;

@Component
@RequiredArgsConstructor
public class AuthCodeVerifyResolver implements HandlerMethodArgumentResolver {
    private final ObjectMapper objectMapper;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(AuthCodeVerifyInject.class) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String requestURI = request.getRequestURI();

        if(requestURI.contains(FIND_ID_URL)) {
            return objectMapper.readValue(getBodyData(request).toString(), IDAuthCodeVerifyRequest.class);
        } else if (requestURI.contains(FIND_PW_URL)){
            return objectMapper.readValue(getBodyData(request).toString(), PWAuthCodeVerifyRequest.class);
        }

        return null;
    }
}
