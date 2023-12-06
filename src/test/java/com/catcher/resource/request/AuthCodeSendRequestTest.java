package com.catcher.resource.request;

import com.catcher.app.AppApplication;
import com.catcher.core.domain.entity.User;
import com.catcher.testconfiguriation.EmbeddedRedisConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static com.catcher.common.BaseResponseStatus.REQUEST_ERROR;
import static com.catcher.resource.request.AuthCodeSendRequest.IDAuthCodeSendRequest;
import static com.catcher.resource.request.AuthCodeSendRequest.PWAuthCodeSendRequest;
import static com.catcher.testconfiguriation.BaseExceptionUtils.assertBaseException;
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
        assertBaseException(
                () -> new IDAuthCodeSendRequest(null).checkValidation(user),
                REQUEST_ERROR
        );

        // [PW Request]
        assertBaseException(
                () -> new PWAuthCodeSendRequest(null, createRandomUUID()).checkValidation(user),
                REQUEST_ERROR
        );

        assertBaseException(
                () -> new PWAuthCodeSendRequest(createRandomUUID(), null).checkValidation(user),
                REQUEST_ERROR
        );
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
        assertBaseException(
                () -> new IDAuthCodeSendRequest(createRandomUUID()).checkValidation(user),
                REQUEST_ERROR
        );

        // [PW Request]
        assertBaseException(
                () -> new PWAuthCodeSendRequest(createRandomUUID(), username).checkValidation(user),
                REQUEST_ERROR
        );
        assertBaseException(
                () -> new PWAuthCodeSendRequest(email, createRandomUUID()).checkValidation(user),
                REQUEST_ERROR
        );
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