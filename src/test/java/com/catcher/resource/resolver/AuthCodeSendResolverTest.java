package com.catcher.resource.resolver;

import com.catcher.app.AppApplication;
import com.catcher.resource.resolver.annotation.AuthCodeSendInject;
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

import static com.catcher.resource.request.AuthCodeSendRequest.IDAuthCodeSendRequest;
import static com.catcher.resource.request.AuthCodeSendRequest.PWAuthCodeSendRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = {AppApplication.class, EmbeddedRedisConfiguration.class})
class AuthCodeSendResolverTest {
    String FIND_ID_URL = "/find-id";
    String FIND_PW_URL = "/find-pw";

    @Autowired
    AuthCodeSendResolver authCodeSendResolver;
    @Mock
    MethodParameter mockMethodParameter;
    @Mock
    AuthCodeSendInject mockAuthCodeSendInjectAnnotation;
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
    @DisplayName("AuthCodeInject 어노테이션이 붙은 파라미터를 지원해야한다.")
    void valid_support() {
        //given

        //when
        when(mockMethodParameter.getParameterAnnotation(AuthCodeSendInject.class)).thenReturn(mockAuthCodeSendInjectAnnotation);

        //then
        assertThat(authCodeSendResolver.supportsParameter(mockMethodParameter)).isTrue();
    }

    @Test
    @DisplayName("AuthCodeInject 어노테이션이 붙지 않은 파라미터는 지원하지 않아야 한다.")
    void invalid_support() {
        //given

        //when
        when(mockMethodParameter.getParameterAnnotation(AuthCodeSendInject.class)).thenReturn(null);

        //then
        assertThat(authCodeSendResolver.supportsParameter(mockMethodParameter)).isFalse();
    }

    @Test
    @DisplayName("IDAuthCodeSendRequest를 바디로 보내면 정상적으로 값이 반환된다.")
    void valid_request_find_id() throws Exception {
        //given
        String email = getRandomUUID();
        IDAuthCodeSendRequest idAuthCodeSendRequest = new IDAuthCodeSendRequest(email);

        //when
        when(mockRequest.getRequestURI()).thenReturn(FIND_ID_URL);
        when(mockNativeWebRequest.getNativeRequest()).thenReturn(mockRequest);
        when(mockRequest.getInputStream()).thenReturn(
                new MockServletInputStream(
                        new ByteArrayInputStream(objectMapper.writeValueAsBytes(idAuthCodeSendRequest))
                )
        );

        //then
        Object resolvedResult = authCodeSendResolver.resolveArgument(
                mockMethodParameter,
                mockModelAndViewContainer,
                mockNativeWebRequest,
                mockWebDataBinderFactory
        );

        assertThat(resolvedResult).isInstanceOf(IDAuthCodeSendRequest.class);
        assertThat(((IDAuthCodeSendRequest) resolvedResult).getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("PWAuthCodeSendRequest를 바디로 보내면 정상적으로 값이 반환된다.")
    void valid_request_find_pw() throws Exception {
        //given
        String email = getRandomUUID();
        String username = getRandomUUID();

        PWAuthCodeSendRequest pwAuthCodeSendRequest = new PWAuthCodeSendRequest(email, username);

        //when
        when(mockRequest.getRequestURI()).thenReturn(FIND_PW_URL);
        when(mockNativeWebRequest.getNativeRequest()).thenReturn(mockRequest);
        when(mockRequest.getInputStream()).thenReturn(
                new MockServletInputStream(
                        new ByteArrayInputStream(objectMapper.writeValueAsBytes(pwAuthCodeSendRequest))
                )
        );

        //then
        Object resolvedResult = authCodeSendResolver.resolveArgument(
                mockMethodParameter,
                mockModelAndViewContainer,
                mockNativeWebRequest,
                mockWebDataBinderFactory
        );

        assertThat(resolvedResult).isInstanceOf(PWAuthCodeSendRequest.class);
        assertThat(((PWAuthCodeSendRequest) resolvedResult).getEmail()).isEqualTo(email);
        assertThat(((PWAuthCodeSendRequest) resolvedResult).getUsername()).isEqualTo(username);
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