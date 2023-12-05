package com.catcher.resource.request;

import com.catcher.app.AppApplication;
import com.catcher.common.exception.BaseException;
import com.catcher.core.domain.entity.User;
import com.catcher.testconfiguriation.EmbeddedRedisConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static com.catcher.resource.request.AuthCodeSendRequest.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = {AppApplication.class, EmbeddedRedisConfiguration.class})
class AuthCodeSendRequestTest {
    @Mock
    User user;

    @Test
    @DisplayName("AuthCodeSendRequest 유효성 체크시 어떤 필드도 비어있으면 예외발생")
    void invalid_request_field_validation() {
        //given
        String email = createRandomUUID();
        //when
        when(user.getEmail()).thenReturn(email);

        //then
        // [ID Request]
        assertThatThrownBy(() ->
                new IDAuthCodeSendRequest(null).checkValidation(user)
        ).isInstanceOf(BaseException.class);

        // [PW Request]
        assertThatThrownBy(() ->
                new PWAuthCodeSendRequest(null, createRandomUUID()).checkValidation(user)
        ).isInstanceOf(BaseException.class);
        assertThatThrownBy(() ->
                new PWAuthCodeSendRequest(createRandomUUID(), null).checkValidation(user)
        ).isInstanceOf(BaseException.class);
    }

    @Test
    @DisplayName("AuthCodeSendRequest 유효성 체크시 아이디나 이메일이 일치하지 않으면 예외발생")
    void not_match_email_username_validation() {
        //given
        String email = createRandomUUID();
        String username = createRandomUUID();

        //when
        when(user.getEmail()).thenReturn(email);
        when(user.getUsername()).thenReturn(username);

        //then

        // [ID Request]
        assertThatThrownBy(() ->
                new IDAuthCodeSendRequest(createRandomUUID()).checkValidation(user)
        ).isInstanceOf(BaseException.class);

        // [PW Request]
        assertThatThrownBy(() ->
                new PWAuthCodeSendRequest(createRandomUUID(), username).checkValidation(user)
        ).isInstanceOf(BaseException.class);
        assertThatThrownBy(() ->
                new PWAuthCodeSendRequest(email, createRandomUUID()).checkValidation(user)
        ).isInstanceOf(BaseException.class);
    }

    @Test
    @DisplayName("AuthCodeSendRequest 유효성 아이디와 이메일이 일치하면 정상")
    void valid_request_validation() {
        //given
        String email = createRandomUUID();
        String username = createRandomUUID();

        //when
        when(user.getEmail()).thenReturn(email);
        when(user.getUsername()).thenReturn(username);

        //then

        // [ID Request]
        new IDAuthCodeSendRequest(email).checkValidation(user);
        // [PW Request]
        new PWAuthCodeSendRequest(email, username).checkValidation(user);
    }

    private String createRandomUUID() {
        return UUID.randomUUID().toString();
    }
}