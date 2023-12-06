package com.catcher.core.service.authcode;

import com.catcher.app.AppApplication;
import com.catcher.common.exception.BaseException;
import com.catcher.core.database.UserRepository;
import com.catcher.core.domain.entity.User;
import com.catcher.core.domain.entity.enums.UserRole;
import com.catcher.core.port.KeyValueDataStorePort;
import com.catcher.resource.request.AuthCodeSendRequest;
import com.catcher.resource.request.AuthCodeVerifyRequest;
import com.catcher.resource.response.PWChangeRequest;
import com.catcher.testconfiguriation.EmbeddedRedisConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static com.catcher.core.domain.entity.enums.UserProvider.CATCHER;
import static com.catcher.resource.request.AuthCodeVerifyRequest.PWAuthCodeVerifyRequest;
import static com.catcher.resource.response.AuthCodeVerifyResponse.IDAuthCodeVerifyResponse;
import static com.catcher.resource.response.AuthCodeVerifyResponse.PWAuthCodeVerifyResponse;
import static com.catcher.utils.KeyGenerator.AuthType;
import static com.catcher.utils.KeyGenerator.AuthType.*;
import static com.catcher.utils.KeyGenerator.generateKey;
import static java.sql.Date.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = {AppApplication.class, EmbeddedRedisConfiguration.class})
@Transactional
class AuthCodeServiceBaseTest {

    @Autowired
    List<AuthCodeServiceBase> authCodeServiceBases;
    @Autowired
    UserRepository userRepository;
    @Autowired
    KeyValueDataStorePort keyValueDataStorePort;
    @PersistenceContext
    EntityManager em;
    @Mock
    AuthCodeSendRequest mockAuthCodeSendRequest;
    @Mock
    AuthCodeVerifyRequest mockAuthCodeVerifyRequest;
    @Mock
    PWAuthCodeVerifyRequest pwAuthCodeVerifyRequest;
    @Mock
    PWChangeRequest mockPWChangeRequest;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Mock
    User mockUser;

    User stubUser;

    @BeforeEach
    void beforeEach() {
        stubUser = createUser();
    }

    @Test
    @DisplayName("FIND_ID / FIND_PW로 조회 시 알맞은 빈이 반환되어야 한다.")
    void find_proper_bean() {
        // given

        // when
        AuthCodeServiceBase idAuthCodeServiceBase = authCodeServiceBases.stream()
                .filter(service -> service.support(FIND_ID))
                .findAny()
                .orElseThrow();

        AuthCodeServiceBase pwAuthCodeServiceBase = authCodeServiceBases.stream()
                .filter(service -> service.support(FIND_PASSWORD))
                .findAny()
                .orElseThrow();

        // then
        assertThat(idAuthCodeServiceBase).isInstanceOf(IDAuthCodeService.class);
        assertThat(pwAuthCodeServiceBase).isInstanceOf(PWAuthCodeService.class);
    }

    @Test
    @DisplayName("존재하지 않는 유저이메일로 키 생성 시, 예외 발생")
    void invalid_email_generate_key() {
        for (AuthCodeServiceBase authCodeService : authCodeServiceBases) {
            // given
            String email = createRandomUUID();

            // when
            when(mockAuthCodeSendRequest.getEmail()).thenReturn(email);

            // then
            assertThatThrownBy(
                    () -> authCodeService.generateAndSaveRandomKey(mockAuthCodeSendRequest)
            ).isInstanceOf(BaseException.class);
        }
    }

    @Test
    @DisplayName("올바른 유저이메일로 키 생성 시 정상 응답 및 레디스에 정상 저장되어야 한다.")
    void valid_email_generate_key() {
        for (AuthCodeServiceBase authCodeServiceBase : authCodeServiceBases) {
            // given
            String email = stubUser.getEmail();

            // when
            when(mockAuthCodeSendRequest.getEmail()).thenReturn(email);

            // then
            String key = authCodeServiceBase.generateAndSaveRandomKey(mockAuthCodeSendRequest);
            assertThat(key).isNotNull();
            assertThat(key.length()).isEqualTo(6);
            assertThat(keyValueDataStorePort.findValidationCodeWithKey(
                    generateKey(stubUser.getId(), authCodeServiceBase.getAuthType()))
            ).isNotNull();
        }
    }

    @Test
    @DisplayName("일치하지 않는 이메일로 코드 인증 요청시 예외발생")
    void invalid_email_verify() {
        for (AuthCodeServiceBase authCodeService : authCodeServiceBases) {
            // given
            User requestUser = stubUser;
            User notMatchedUser = createUser();

            // when
            when(mockAuthCodeSendRequest.getEmail()).thenReturn(requestUser.getEmail());
            when(mockAuthCodeVerifyRequest.getEmail()).thenReturn(notMatchedUser.getEmail());
            authCodeService.generateAndSaveRandomKey(mockAuthCodeSendRequest);

            // then
            assertThatThrownBy(
                    () -> authCodeService.verifyAuthCode(mockAuthCodeVerifyRequest)
            ).isInstanceOf(BaseException.class);
        }
    }

    @Test
    @DisplayName("일치하지 않는 아이디로 코드 인증 요청시 예외발생")
    void invalid_username_verify() {
        // given
        AuthCodeServiceBase pwAuthCodeServiceBase = authCodeServiceBases.stream()
                .filter(service -> service.support(FIND_PASSWORD))
                .findAny()
                .orElseThrow();

        // when
        when(pwAuthCodeVerifyRequest.getEmail()).thenReturn(stubUser.getEmail());
        when(pwAuthCodeVerifyRequest.getUsername()).thenReturn(createRandomUUID());

        // then
        assertThatThrownBy(
                () -> pwAuthCodeServiceBase.verifyAuthCode(pwAuthCodeVerifyRequest)
        ).isInstanceOf(BaseException.class);
    }

    @Test
    @DisplayName("인증코드가 일치하지 않으면 예외발생")
    void invalid_request_not_match_auth_code() {
        for (AuthCodeServiceBase authCodeServiceBase : authCodeServiceBases) {
            // given
            when(pwAuthCodeVerifyRequest.getEmail()).thenReturn(stubUser.getEmail());
            when(pwAuthCodeVerifyRequest.getUsername()).thenReturn(stubUser.getUsername());
            when(pwAuthCodeVerifyRequest.getAuthCode()).thenReturn(createRandomUUID());
            when(mockAuthCodeSendRequest.getEmail()).thenReturn(stubUser.getEmail());
            String answer = authCodeServiceBase.generateAndSaveRandomKey(mockAuthCodeSendRequest);

            // when

            // then
            assertThatThrownBy(
                    () -> authCodeServiceBase.verifyAuthCode(mockAuthCodeVerifyRequest)
            ).isInstanceOf(BaseException.class);
        }
    }

    @Test
    @DisplayName("ID 찾기 인증코드가 이메일, 인증코드가 일치하면 정상 응답 및 레디스 키 삭제 되어야한다")
    void id_find_match_all() {
        // given
        AuthCodeServiceBase authCodeServiceBase = getAuthCodeService(FIND_ID);

        when(mockAuthCodeSendRequest.getEmail()).thenReturn(stubUser.getEmail());
        String answer = authCodeServiceBase.generateAndSaveRandomKey(mockAuthCodeSendRequest);

        when(mockAuthCodeVerifyRequest.getEmail()).thenReturn(stubUser.getEmail());
        when(mockAuthCodeVerifyRequest.getAuthCode()).thenReturn(answer);

        // when

        // then
        IDAuthCodeVerifyResponse authCodeVerifyResponse = (IDAuthCodeVerifyResponse) authCodeServiceBase.verifyAuthCode(mockAuthCodeVerifyRequest);

        assertThat(authCodeVerifyResponse.getCreatedAt()).isEqualTo(from(stubUser.getCreatedAt().toInstant()));
        assertThat(authCodeVerifyResponse.getUsername()).isEqualTo(stubUser.getUsername());
        assertThatThrownBy(
                () -> keyValueDataStorePort.findValidationCodeWithKey(generateKey(stubUser.getEmail(), FIND_ID))
        ).isInstanceOf(BaseException.class);
    }

    @Test
    @DisplayName("PW 찾기 인증코드가 이메일, 인증코드가 일치하면 정상 응답 및 레디스 키 삭제 되어야한다")
    void pw_find_match_all() {
        // given
        AuthCodeServiceBase authCodeServiceBase = getAuthCodeService(FIND_PASSWORD);
        when(pwAuthCodeVerifyRequest.getEmail()).thenReturn(stubUser.getEmail());
        when(pwAuthCodeVerifyRequest.getUsername()).thenReturn(stubUser.getUsername());
        when(mockAuthCodeSendRequest.getEmail()).thenReturn(stubUser.getEmail());
        String answer = authCodeServiceBase.generateAndSaveRandomKey(mockAuthCodeSendRequest);
        when(pwAuthCodeVerifyRequest.getAuthCode()).thenReturn(answer);
        PWAuthCodeVerifyResponse authCodeVerifyResponse = (PWAuthCodeVerifyResponse) authCodeServiceBase.verifyAuthCode(pwAuthCodeVerifyRequest);

        // when

        // then
        assertThat(authCodeVerifyResponse.getCode()).isNotNull();
        assertThat(
                keyValueDataStorePort.findValidationCodeWithKey(
                        generateKey(authCodeVerifyResponse.getCode(), FIND_PASSWORD_SUCCESS)
                )
        ).isEqualTo(stubUser.getEmail());
    }

    @Test
    @DisplayName("PW - 존재하지 않는 코드로 비밀번호 변경 요청 시 예외 발생")
    void invalid_pw_code_pw_change() {
        // given
        String previousPassword = new String(stubUser.getPassword());
        String toChangePassword = createRandomUUID();

        AuthCodeServiceBase authCodeService = getAuthCodeService(FIND_PASSWORD);
        when(pwAuthCodeVerifyRequest.getEmail()).thenReturn(stubUser.getEmail());
        when(pwAuthCodeVerifyRequest.getUsername()).thenReturn(stubUser.getUsername());
        when(mockAuthCodeSendRequest.getEmail()).thenReturn(stubUser.getEmail());
        String answer = authCodeService.generateAndSaveRandomKey(mockAuthCodeSendRequest);
        when(pwAuthCodeVerifyRequest.getAuthCode()).thenReturn(answer);
        String code = ((PWAuthCodeVerifyResponse) authCodeService.verifyAuthCode(pwAuthCodeVerifyRequest)).getCode();

        // when
        when(mockPWChangeRequest.getCode()).thenReturn(createRandomUUID());
        when(mockPWChangeRequest.getNewPassword()).thenReturn(toChangePassword);
        when(mockPWChangeRequest.getNewPasswordCheck()).thenReturn(toChangePassword);

        // then
        assertThatThrownBy(
                () -> authCodeService.changePassword(mockPWChangeRequest)
        ).isInstanceOf(BaseException.class);
    }

    @Test
    @DisplayName("PW - 정상 코드로 비밀번호 변경시 정상 변경되어야 한다.")
    void valid_pw_change() {
        // given
        String previousPassword = new String(stubUser.getPassword());
        String toChangePassword = createRandomUUID();

        AuthCodeServiceBase authCodeService = getAuthCodeService(FIND_PASSWORD);
        when(pwAuthCodeVerifyRequest.getEmail()).thenReturn(stubUser.getEmail());
        when(pwAuthCodeVerifyRequest.getUsername()).thenReturn(stubUser.getUsername());
        when(mockAuthCodeSendRequest.getEmail()).thenReturn(stubUser.getEmail());
        String answer = authCodeService.generateAndSaveRandomKey(mockAuthCodeSendRequest);
        when(pwAuthCodeVerifyRequest.getAuthCode()).thenReturn(answer);
        String code = ((PWAuthCodeVerifyResponse) authCodeService.verifyAuthCode(pwAuthCodeVerifyRequest)).getCode();

        // when
        when(mockPWChangeRequest.getCode()).thenReturn(code);
        when(mockPWChangeRequest.getNewPassword()).thenReturn(toChangePassword);
        when(mockPWChangeRequest.getNewPasswordCheck()).thenReturn(toChangePassword);
        authCodeService.changePassword(mockPWChangeRequest);
        flushAndClearPersistence();

        // then
        User changedPasswordUser = userRepository.findByEmail(stubUser.getEmail()).orElseThrow();
        assertThat(passwordEncoder.matches(toChangePassword, changedPasswordUser.getPassword())).isTrue();
    }

    private AuthCodeServiceBase getAuthCodeService(AuthType authType) {
        return authCodeServiceBases.stream()
                .filter(a -> a.getAuthType().equals(authType))
                .findAny()
                .orElseThrow();
    }

    private void flushAndClearPersistence() {
        em.flush();
        em.clear();
    }

    private User createUser() {
        var user = createUser(createRandomUUID(), createRandomUUID(), createRandomUUID(), createRandomUUID());
        User save = userRepository.save(user);
        flushAndClearPersistence();
        return save;
    }

    private User createUser(String username, String phone, String email, String nickname) {
        return User.builder()
                .username(username)
                .password(createRandomUUID())
                .phone(phone)
                .email(email)
                .profileImageUrl(null)
                .introduceContent(null)
                .nickname(nickname)
                .userProvider(CATCHER)
                .userRole(UserRole.USER)
                .userAgeTerm(ZonedDateTime.now())
                .userServiceTerm(ZonedDateTime.now())
                .userPrivacyTerm(ZonedDateTime.now())
                .emailMarketingTerm(ZonedDateTime.now())
                .phoneMarketingTerm(ZonedDateTime.now())
                .build();
    }

    private String createRandomUUID() {
        return UUID.randomUUID().toString();
    }
}