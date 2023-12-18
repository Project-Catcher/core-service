package com.catcher.core.service;

import com.catcher.app.AppApplication;
import com.catcher.common.exception.BaseException;
import com.catcher.core.database.DBManager;
import com.catcher.core.database.UserRepository;
import com.catcher.core.domain.entity.User;
import com.catcher.core.domain.entity.enums.UserRole;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.user.UserCreateRequest;
import com.catcher.core.dto.user.UserLoginRequest;
import com.catcher.testconfiguriation.EmbeddedRedisConfiguration;
import com.catcher.utils.KeyGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.catcher.core.domain.entity.enums.UserProvider.*;
import static com.catcher.utils.KeyGenerator.AuthType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest(classes = {AppApplication.class, EmbeddedRedisConfiguration.class})
@Transactional
class UserServiceTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserService userService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    DBManager dbManager;
    @PersistenceContext
    EntityManager em;

    @DisplayName("정상 회원 가입 시, 조회가 가능하고, 토큰이 반환된다.")
    @Test
    void valid_sign_up() {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(createRandomUUID(), createRandomUUID(), createRandomUUID(), createRandomUUID());

        //when
        TokenDto tokenDto = userService.signUpUser(userCreateRequest);
        flushAndClearPersistence();

        //then
        assertThat(tokenDto.getAccessToken()).isNotEmpty();
        assertThat(tokenDto.getRefreshToken()).isNotEmpty();
        assertThat(userRepository.findByEmail(userCreateRequest.getEmail())).isPresent();
        assertThat(userRepository.findByNickname(userCreateRequest.getNickname())).isPresent();
        assertThat(userRepository.findByPhone(userCreateRequest.getPhone())).isPresent();
        assertThat(userRepository.findByUsername(userCreateRequest.getUsername())).isPresent();
    }

    @DisplayName("동일한 아이디로 가입 시, 예외 발생")
    @Test
    void same_username_sign_up() {
        //given
        User user = createUser(createRandomUUID(), createRandomUUID(), createRandomUUID(), createRandomUUID());
        UserCreateRequest userCreateRequest = userCreateRequest(user.getUsername(), createRandomUUID(), createRandomUUID(), createRandomUUID());
        userRepository.save(user);

        //when

        //then
        assertThatThrownBy(() -> userService.signUpUser(userCreateRequest))
                .isInstanceOf(BaseException.class);
    }

    @DisplayName("동일한 이메일로 가입 시, 예외 발생")
    @Test
    void same_email_sign_up() {
        //given
        User user = createUser(createRandomUUID(), createRandomUUID(), createRandomUUID(), createRandomUUID());
        UserCreateRequest userCreateRequest = userCreateRequest(createRandomUUID(), user.getEmail(), createRandomUUID(), createRandomUUID());
        userRepository.save(user);

        //when

        //then
        assertThatThrownBy(() -> userService.signUpUser(userCreateRequest))
                .isInstanceOf(BaseException.class);
    }

    @DisplayName("동일한 핸드폰으로 가입 시, 예외 발생")
    @Test
    void same_phone_sign_up() {
        //given
        User user = createUser(createRandomUUID(), createRandomUUID(), createRandomUUID(), createRandomUUID());
        UserCreateRequest userCreateRequest = userCreateRequest(createRandomUUID(), createRandomUUID(), createRandomUUID(), user.getPhone());
        userRepository.save(user);

        //when

        //then
        assertThatThrownBy(() -> userService.signUpUser(userCreateRequest))
                .isInstanceOf(BaseException.class);
    }

    @DisplayName("동일한 닉네임으로 가입 시, 예외 발생")
    @Test
    void same_nickname_sign_up() {
        //given
        User user = createUser(createRandomUUID(), createRandomUUID(), createRandomUUID(), createRandomUUID());
        UserCreateRequest userCreateRequest = userCreateRequest(createRandomUUID(), createRandomUUID(), user.getNickname(), createRandomUUID());
        userRepository.save(user);

        //when

        //then
        assertThatThrownBy(() -> userService.signUpUser(userCreateRequest))
                .isInstanceOf(BaseException.class);
    }

    @DisplayName("정상 로그인 시, 토큰이 반환된다.")
    @Test
    void valid_login() {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(createRandomUUID(), createRandomUUID(), createRandomUUID(), createRandomUUID());
        userService.signUpUser(userCreateRequest);
        flushAndClearPersistence();

        //when
        UserLoginRequest userLoginRequest = userLoginRequest(userCreateRequest.getUsername(), userCreateRequest.getPassword());
        TokenDto tokenDto = userService.login(userLoginRequest);

        //then
        assertThat(tokenDto.getAccessToken()).isNotEmpty();
        assertThat(tokenDto.getRefreshToken()).isNotEmpty();
    }

    @DisplayName("카카오로 회원가입한 계정으로 로그인 시도 시 예외 발생")
    @Test
    void invalid_login_by_kakao_account() {
        //given
        String username = createRandomUUID();
        String password = createRandomUUID();
        User oAuthUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .phone(createRandomUUID())
                .email(createRandomUUID())
                .profileImageUrl(null)
                .introduceContent(null)
                .nickname(createRandomUUID())
                .userProvider(KAKAO)
                .userRole(UserRole.USER)
                .userAgeTerm(LocalDateTime.now())
                .userServiceTerm(LocalDateTime.now())
                .userPrivacyTerm(LocalDateTime.now())
                .emailMarketingTerm(LocalDateTime.now())
                .phoneMarketingTerm(LocalDateTime.now())
                .build();
        userRepository.save(oAuthUser);
        flushAndClearPersistence();

        //when
        UserLoginRequest userLoginRequest = userLoginRequest(username, password);

        //then
        assertThatThrownBy(() -> userService.login(userLoginRequest))
                .isInstanceOf(BaseException.class);
    }

    @DisplayName("네이버로 회원가입한 계정으로 로그인 시도 시 예외 발생")
    @Test
    void invalid_login_by_naver_account() {
        //given
        String username = createRandomUUID();
        String password = createRandomUUID();
        User oAuthUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .phone(createRandomUUID())
                .email(createRandomUUID())
                .profileImageUrl(null)
                .introduceContent(null)
                .nickname(createRandomUUID())
                .userProvider(NAVER)
                .userRole(UserRole.USER)
                .userAgeTerm(LocalDateTime.now())
                .userServiceTerm(LocalDateTime.now())
                .userPrivacyTerm(LocalDateTime.now())
                .emailMarketingTerm(LocalDateTime.now())
                .phoneMarketingTerm(LocalDateTime.now())
                .build();
        userRepository.save(oAuthUser);
        flushAndClearPersistence();

        //when
        UserLoginRequest userLoginRequest = userLoginRequest(username, password);

        //then
        assertThatThrownBy(() -> userService.login(userLoginRequest))
                .isInstanceOf(BaseException.class);
    }

    @DisplayName("유효하지 않은 아이디로 로그인 시, 예외 발생")
    @Test
    void invalid_username_login() {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(createRandomUUID(), createRandomUUID(), createRandomUUID(), createRandomUUID());
        userService.signUpUser(userCreateRequest);
        flushAndClearPersistence();

        //when
        UserLoginRequest userLoginRequest = userLoginRequest(createRandomUUID(), userCreateRequest.getPassword());

        //then
        assertThatThrownBy(() -> userService.login(userLoginRequest))
                .isInstanceOf(BaseException.class);
    }

    @DisplayName("유효하지 않은 비밀번호로 로그인 시, 예외 발생")
    @Test
    void invalid_password_login() {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(createRandomUUID(), createRandomUUID(), createRandomUUID(), createRandomUUID());
        userService.signUpUser(userCreateRequest);
        flushAndClearPersistence();

        //when
        UserLoginRequest userLoginRequest = userLoginRequest(userCreateRequest.getUsername(), createRandomUUID());

        //then
        assertThatThrownBy(() -> userService.login(userLoginRequest))
                .isInstanceOf(BaseException.class);
    }

    @DisplayName("유효하지 않은 아이디,비밀번호로 로그인 시, 예외 발생")
    @Test
    void invalid_username_password_login() {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(createRandomUUID(), createRandomUUID(), createRandomUUID(), createRandomUUID());
        userService.signUpUser(userCreateRequest);
        flushAndClearPersistence();

        //when
        UserLoginRequest userLoginRequest = userLoginRequest(createRandomUUID(), createRandomUUID());

        //then
        assertThatThrownBy(() -> userService.login(userLoginRequest))
                .isInstanceOf(BaseException.class);
    }

    @DisplayName("캐쳐 회원가입 시, 핸드폰 인증이 완료되어있어야 한다.")
    @Test
    void sign_up_need_phone_authentication() {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(createRandomUUID(), createRandomUUID(), createRandomUUID(), createRandomUUID());
        //when
        userService.signUpUser(userCreateRequest);
        flushAndClearPersistence();

        //then
        User user = userRepository.findByEmail(userCreateRequest.getEmail()).orElseThrow();
        assertThat(user.getUserProvider()).isEqualTo(CATCHER);
        assertThat(user.getPhoneAuthentication()).isNotNull();
    }

    @DisplayName("정상 로그아웃 시, 블랙리스트 토큰에 저장되어야 한다.")
    @Test
    void valid_logout() {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(createRandomUUID(), createRandomUUID(), createRandomUUID(), createRandomUUID());
        TokenDto tokenDto = userService.signUpUser(userCreateRequest);
        flushAndClearPersistence();
        //when
        userService.logout("Bearer " + tokenDto.getAccessToken(), tokenDto.getRefreshToken());
        //then
        Optional<String> value = dbManager.getValue(KeyGenerator.generateKey(tokenDto.getAccessToken(), BLACK_LIST_ACCESS_TOKEN));
        assertThat(value).isPresent();
    }

    //region PRIVATE METHOD
    private UserLoginRequest userLoginRequest(String username,String password) {
        return new UserLoginRequest(username, password);
    }

    private UserCreateRequest userCreateRequest(String username, String email, String nickname, String phone) {
        return UserCreateRequest.builder()
                .nickname(nickname)
                .ageTerm(LocalDateTime.now())
                .serviceTerm(LocalDateTime.now())
                .marketingTerm(LocalDateTime.now())
                .privacyTerm(LocalDateTime.now())
                .phone(phone)
                .email(email)
                .username(username)
                .password(createRandomUUID())
                .build();
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
                .userAgeTerm(LocalDateTime.now())
                .userServiceTerm(LocalDateTime.now())
                .userPrivacyTerm(LocalDateTime.now())
                .emailMarketingTerm(LocalDateTime.now())
                .phoneMarketingTerm(LocalDateTime.now())
                .build();
    }

    private void flushAndClearPersistence() {
        em.flush();
        em.clear();
    }

    private String createRandomUUID() {
        return UUID.randomUUID().toString();
    }
    //endregion
}