package com.catcher.core.service;

import cn.apiclub.captcha.Captcha;
import com.catcher.app.AppApplication;
import com.catcher.common.exception.BaseException;
import com.catcher.core.database.UserRepository;
import com.catcher.core.domain.entity.User;
import com.catcher.core.domain.entity.enums.UserRole;
import com.catcher.core.port.KeyValueDataStorePort;
import com.catcher.testconfiguriation.EmbeddedRedisConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

import static com.catcher.core.domain.entity.enums.UserProvider.CATCHER;
import static com.catcher.utils.KeyGenerator.AuthType;
import static com.catcher.utils.KeyGenerator.AuthType.FIND_ID;
import static com.catcher.utils.KeyGenerator.AuthType.FIND_PASSWORD;
import static com.catcher.utils.KeyGenerator.generateKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest(classes = {AppApplication.class, EmbeddedRedisConfiguration.class})
@Transactional
class CaptchaServiceTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    KeyValueDataStorePort keyValueDataStorePort;
    @Autowired
    CaptchaService captchaService;
    @PersistenceContext
    EntityManager em;

    AuthType[] authTypes = {FIND_ID, FIND_PASSWORD};

    @Test
    @DisplayName("존재하지 않는 이메일로 캡챠 요청시 예외 발생")
    void invalid_user_generate_captcha() {
        for (AuthType authType : authTypes) {
            // given
            User user = createUser();

            // when

            // then
            assertThatThrownBy(
                    () -> captchaService.generateCaptchaAndSaveAnswer(createRandomUUID(), authType)
            ).isInstanceOf(BaseException.class);
        }
    }

    @Test
    @DisplayName("올바른 이메일로 캡챠 요청시 정상 응답 및 레디스에 저장된 정답과 일치하여야 한다.")
    void valid_generate_captcha() {
        for (AuthType authType : authTypes) {
            // given
            User user = createUser();

            // when
            Captcha captcha = captchaService.generateCaptchaAndSaveAnswer(user.getEmail(), authType);

            // then
            assertThat(captcha).isNotNull();
            assertThat(keyValueDataStorePort.findValidationCodeWithKey(generateKey(user.getId(), authType))).isNotNull();
            assertThat(keyValueDataStorePort.findValidationCodeWithKey(generateKey(user.getId(), authType))).isEqualTo(captcha.getAnswer());
        }
    }

    @Test
    @DisplayName("올바른 이메일로 캡챠 요청시 정상 응답 및 레디스에 저장된 정답과 일치하여야 한다.")
    void valid_verify_captcha() {
        for (AuthType authType : authTypes) {
            // given
            User user = createUser();

            // when
            Captcha captcha = captchaService.generateCaptchaAndSaveAnswer(user.getEmail(), authType);

            // then
            assertThat(captcha).isNotNull();
            assertThat(keyValueDataStorePort.findValidationCodeWithKey(generateKey(user.getId(), authType))).isNotNull();
            assertThat(keyValueDataStorePort.findValidationCodeWithKey(generateKey(user.getId(), authType))).isEqualTo(captcha.getAnswer());
        }
    }

    @Test
    @DisplayName("정답이 다르면 예외가 발생해야한다.")
    void invalid_answer_verify_captcha() {
        for (AuthType authType : authTypes) {
            // given
            User user = createUser();
            Captcha captcha = captchaService.generateCaptchaAndSaveAnswer(user.getEmail(), authType);
            // when

            // then
            assertThatThrownBy(
                    () -> captchaService.validateCaptcha(user.getEmail(), captcha.getAnswer() + "1", authType)
            ).isInstanceOf(BaseException.class);
        }
    }

    @Test
    @DisplayName("정답이 맞다면 예외가 발생하지 않고, 코드가 레디스에서 삭제되어야한다.")
    void valid_answer_verify_captcha() {
        for (AuthType authType : authTypes) {
            // given
            User user = createUser();
            Captcha captcha = captchaService.generateCaptchaAndSaveAnswer(user.getEmail(), authType);
            // when

            // then
            captchaService.validateCaptcha(user.getEmail(), captcha.getAnswer(), authType);
            assertThatThrownBy(
                    () -> keyValueDataStorePort.findValidationCodeWithKey(
                            generateKey(user.getId(), authType)
                    )
            ).isInstanceOf(BaseException.class);
        }
    }

    private User createUser() {
        User user = User.builder()
                .username(createRandomUUID())
                .password(createRandomUUID())
                .phone(createRandomUUID())
                .email(createRandomUUID())
                .profileImageUrl(null)
                .introduceContent(null)
                .nickname(createRandomUUID())
                .userProvider(CATCHER)
                .userRole(UserRole.USER)
                .userAgeTerm(ZonedDateTime.now())
                .userServiceTerm(ZonedDateTime.now())
                .userPrivacyTerm(ZonedDateTime.now())
                .emailMarketingTerm(ZonedDateTime.now())
                .phoneMarketingTerm(ZonedDateTime.now())
                .build();
        User save = userRepository.save(user);
        em.flush();
        em.clear();
        return save;
    }

    private String createRandomUUID() {
        return UUID.randomUUID().toString();
    }
}