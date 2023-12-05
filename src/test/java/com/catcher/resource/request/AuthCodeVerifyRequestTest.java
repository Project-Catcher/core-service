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

import static com.catcher.resource.request.AuthCodeVerifyRequest.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = {AppApplication.class, EmbeddedRedisConfiguration.class})
class AuthCodeVerifyRequestTest {
    @Mock
    User user;

    @Test
    @DisplayName("유효값 체크시 어떤 필드도 비어있으면 예외가 발생한다.")
    void invalid_request_field_validation() {
        // [ID Request]
        // 이메일이 비어있는 경우
        assertThatThrownBy(() ->
                new IDAuthCodeVerifyRequest(null, createRandomUUID()).checkValidation(user, createRandomUUID())
        ).isInstanceOf(BaseException.class);
        // 코드가 비어있는 경우
        assertThatThrownBy(() ->
                new IDAuthCodeVerifyRequest(createRandomUUID(), null).checkValidation(user, createRandomUUID())
        ).isInstanceOf(BaseException.class);

        // [PW Request]
        // 이메일이 비어있는 경우
        assertThatThrownBy(() ->
                new PWAuthCodeVerifyRequest(null, createRandomUUID(), createRandomUUID()).checkValidation(user, createRandomUUID())
        ).isInstanceOf(BaseException.class);
        // 아이디가 비어있는 경우
        assertThatThrownBy(() ->
                new PWAuthCodeVerifyRequest(createRandomUUID(), null, createRandomUUID()).checkValidation(user, createRandomUUID())
        ).isInstanceOf(BaseException.class);
        // 코드가 비어있는 경우
        assertThatThrownBy(() ->
                new PWAuthCodeVerifyRequest(createRandomUUID(), createRandomUUID(), null).checkValidation(user, createRandomUUID())
        ).isInstanceOf(BaseException.class);
    }

    @Test
    @DisplayName("정답이 일치하지 않으면 예외가 발생한다.")
    void not_match_answer_validation() {
        //given
        String answer = createRandomUUID();

        //when

        //then
        // [ID Request]
        assertThatThrownBy(() ->
                new IDAuthCodeVerifyRequest(createRandomUUID(), createRandomUUID()).checkValidation(user, answer)
        ).isInstanceOf(BaseException.class);

        // [PW Request]
        assertThatThrownBy(() ->
                new PWAuthCodeVerifyRequest(createRandomUUID(), createRandomUUID(), createRandomUUID()).checkValidation(user, answer)
        ).isInstanceOf(BaseException.class);
    }

    @Test
    @DisplayName("이메일이나 아이디가 일치하지 않으면 예외가 발생한다.")
    void not_match_email_username_validation() {
        //given
        String email = createRandomUUID();
        String answer = createRandomUUID();
        String username = createRandomUUID();

        //when
        when(user.getEmail()).thenReturn(email);
        when(user.getUsername()).thenReturn(username);

        //then
        // [ID Request]
        assertThatThrownBy(() ->
                new IDAuthCodeVerifyRequest(createRandomUUID(), answer).checkValidation(user, answer)
        ).isInstanceOf(BaseException.class);

        // [PW Request]
        assertThatThrownBy(() ->
                new PWAuthCodeVerifyRequest(createRandomUUID(), username, answer).checkValidation(user, answer)
        ).isInstanceOf(BaseException.class);
        assertThatThrownBy(() ->
                new PWAuthCodeVerifyRequest(email, createRandomUUID(), answer).checkValidation(user, answer)
        ).isInstanceOf(BaseException.class);
    }

    @Test
    @DisplayName("이메일, 아이디, 정답이 모두 일치하면 정상처리")
    void valid_request() {
        //given
        String email = createRandomUUID();
        String answer = createRandomUUID();
        String username = createRandomUUID();

        //when
        when(user.getEmail()).thenReturn(email);
        when(user.getUsername()).thenReturn(username);

        //then
        // [ID Request]
        new IDAuthCodeVerifyRequest(email, answer).checkValidation(user, answer);

        // [PW Request]
        new PWAuthCodeVerifyRequest(email, username, answer).checkValidation(user, answer);
        new PWAuthCodeVerifyRequest(email, username, answer).checkValidation(user, answer);
    }

    private String createRandomUUID() {
        return UUID.randomUUID().toString();
    }
}