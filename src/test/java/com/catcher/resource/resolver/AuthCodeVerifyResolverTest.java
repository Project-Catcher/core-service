package com.catcher.resource.resolver;

import com.catcher.app.AppApplication;
import com.catcher.resource.resolver.annotation.AuthCodeVerifyInject;
import com.catcher.testconfiguriation.EmbeddedRedisConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.MethodParameter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import static com.catcher.resource.request.AuthCodeVerifyRequest.IDAuthCodeVerifyRequest;
import static com.catcher.resource.request.AuthCodeVerifyRequest.PWAuthCodeVerifyRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = {AppApplication.class, EmbeddedRedisConfiguration.class})
class AuthCodeVerifyResolverTest {
    String FIND_ID_URL = "/find-id";
    String FIND_PW_URL = "/find-pw";

    @Autowired
    AuthCodeVerifyResolver authCodeVerifyResolver;
    @Mock
    MethodParameter mockMethodParameter;
    @Mock
    AuthCodeVerifyInject mockAuthCodeSendInjectAnnotation;
    @Mock
    ModelAndViewContainer mockModelAndViewContainer;
    @Mock
    HttpServletRequest mockRequest;
    @Mock
    NativeWebRequest mockNativeWebRequest;
    @Mock
    WebDataBinderFactory mockWebDataBinderFactory;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("AuthCodeVerifyInject 어노테이션이 붙은 파라미터를 지원해야한다.")
    void valid_support() {
        //given
        when(mockMethodParameter.getParameterAnnotation(AuthCodeVerifyInject.class)).thenReturn(mockAuthCodeSendInjectAnnotation);

        //when

        //then
        assertThat(authCodeVerifyResolver.supportsParameter(mockMethodParameter)).isTrue();
    }

    @Test
    @DisplayName("AuthCodeVerifyInject 어노테이션이 붙지 않은 파라미터는 지원하지 않아야 한다.")
    void invalid_support() {
        //given
        when(mockMethodParameter.getParameterAnnotation(AuthCodeVerifyInject.class)).thenReturn(null);

        //when

        //then
        assertThat(authCodeVerifyResolver.supportsParameter(mockMethodParameter)).isFalse();
    }

    @Test
    @DisplayName("IDAuthCodeVerifyRequest를 바디로 보내면 정상적으로 값이 반환된다.")
    void valid_request_find_id() throws Exception {
        //given
        String email = getRandomUUID();
        String authCode = getRandomUUID();
        IDAuthCodeVerifyRequest idAuthCodeVerifyRequest = new IDAuthCodeVerifyRequest(email, authCode);


        //when
        when(mockRequest.getRequestURI()).thenReturn(FIND_ID_URL);
        when(mockNativeWebRequest.getNativeRequest()).thenReturn(mockRequest);
        when(mockRequest.getInputStream()).thenReturn(
                new MockServletInputStream(
                        new ByteArrayInputStream(objectMapper.writeValueAsBytes(idAuthCodeVerifyRequest))
                )
        );

        //then
        Object resolvedResult = authCodeVerifyResolver.resolveArgument(
                mockMethodParameter,
                mockModelAndViewContainer,
                mockNativeWebRequest,
                mockWebDataBinderFactory
        );

        assertThat(resolvedResult).isInstanceOf(IDAuthCodeVerifyRequest.class);
        assertThat(((IDAuthCodeVerifyRequest) resolvedResult).getEmail()).isEqualTo(email);
        assertThat(((IDAuthCodeVerifyRequest) resolvedResult).getAuthCode()).isEqualTo(authCode);
    }

    @Test
    @DisplayName("PWAuthCodeSendRequest를 바디로 보내면 정상적으로 값이 반환된다.")
    void valid_request_find_pw() throws Exception {
        //given
        String email = getRandomUUID();
        String username = getRandomUUID();
        String authCode = getRandomUUID();
        PWAuthCodeVerifyRequest pwAuthCodeVerifyRequest = new PWAuthCodeVerifyRequest(email, username, authCode);


        //when
        when(mockRequest.getRequestURI()).thenReturn(FIND_PW_URL);
        when(mockNativeWebRequest.getNativeRequest()).thenReturn(mockRequest);
        when(mockRequest.getInputStream()).thenReturn(
                new MockServletInputStream(
                        new ByteArrayInputStream(objectMapper.writeValueAsBytes(pwAuthCodeVerifyRequest))
                )
        );

        //then
        Object resolvedResult = authCodeVerifyResolver.resolveArgument(
                mockMethodParameter,
                mockModelAndViewContainer,
                mockNativeWebRequest,
                mockWebDataBinderFactory
        );

        assertThat(resolvedResult).isInstanceOf(PWAuthCodeVerifyRequest.class);
        assertThat(((PWAuthCodeVerifyRequest) resolvedResult).getEmail()).isEqualTo(email);
        assertThat(((PWAuthCodeVerifyRequest) resolvedResult).getUsername()).isEqualTo(username);
        assertThat(((PWAuthCodeVerifyRequest) resolvedResult).getAuthCode()).isEqualTo(authCode);
    }

    private String getRandomUUID() {
        return UUID.randomUUID().toString();
    }

    private static class MockServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;

        public MockServletInputStream(ByteArrayInputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public int read() {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setReadListener(ReadListener listener) {

        }
    }
}