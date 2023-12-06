package com.catcher.resource.response;

import com.catcher.app.AppApplication;
import com.catcher.common.exception.BaseException;
import com.catcher.testconfiguriation.EmbeddedRedisConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest(classes = {AppApplication.class, EmbeddedRedisConfiguration.class})
class PWChangeRequestTest {

    @Test
    @DisplayName("어떤 필드로 비어있으면 예외 발생")
    void invalid_field_request_validation() {
        // given
        String code = createRandomUUID();
        String newPassword = createRandomUUID();
        String newPasswordCheck = createRandomUUID();
        // when

        // then
        assertThatThrownBy(
                () -> new PWChangeRequest(null, newPassword, newPasswordCheck).checkValidation()
        ).isInstanceOf(BaseException.class);
        assertThatThrownBy(
                () -> new PWChangeRequest(code, null, newPasswordCheck).checkValidation()
        ).isInstanceOf(BaseException.class);
        assertThatThrownBy(
                () -> new PWChangeRequest(code, newPassword, null).checkValidation()
        ).isInstanceOf(BaseException.class);
    }

    @Test
    @DisplayName("비밀번호와 비밀번호 확인 필드가 일치하지 않으면 예외 발생")
    void not_match_password_validation() {
        // given
        String code = createRandomUUID();
        String newPassword = createRandomUUID();
        String newPasswordCheck = createRandomUUID();
        // when

        // then
        assertThatThrownBy(
                () -> new PWChangeRequest(code, newPassword, newPasswordCheck).checkValidation()
        ).isInstanceOf(BaseException.class);
    }

    @Test
    @DisplayName("비밀번호와 비밀번호 확인 필드가 일치하고, 모든 필드가 비어 있지 않으면 정상")
    void valid_request() {
        // given
        String code = createRandomUUID();
        String newPassword = createRandomUUID();
        String newPasswordCheck = new String(newPassword);

        // when

        // then
        new PWChangeRequest(code, newPassword, newPasswordCheck).checkValidation();
    }

    private String createRandomUUID() {
        return UUID.randomUUID().toString();
    }
}