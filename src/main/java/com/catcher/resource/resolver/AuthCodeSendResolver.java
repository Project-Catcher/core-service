package com.catcher.resource.resolver;

import com.catcher.resource.resolver.annotation.AuthCodeSendInject;
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
import static com.catcher.resource.request.AuthCodeSendRequest.IDAuthCodeSendRequest;
import static com.catcher.resource.request.AuthCodeSendRequest.PWAuthCodeSendRequest;
import static com.catcher.utils.HttpServletUtils.getBodyData;

@Component
@RequiredArgsConstructor
public class AuthCodeSendResolver implements HandlerMethodArgumentResolver {
    private final ObjectMapper objectMapper;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return getAnnotation(parameter) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String requestURI = request.getRequestURI();

        if(requestURI.contains(FIND_ID_URL)) {
            return objectMapper.readValue(getBodyData(request).toString(), IDAuthCodeSendRequest.class);
        } else if (requestURI.contains(FIND_PW_URL)){
            return objectMapper.readValue(getBodyData(request).toString(), PWAuthCodeSendRequest.class);
        }

        return null;
    }

    private AuthCodeSendInject getAnnotation(MethodParameter methodParameter) {
        return methodParameter.getParameterAnnotation(AuthCodeSendInject.class);
    }
}
