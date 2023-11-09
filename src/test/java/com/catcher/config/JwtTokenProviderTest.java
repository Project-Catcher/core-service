package com.catcher.config;

import com.catcher.app.AppApplication;
import com.catcher.common.exception.BaseException;
import com.catcher.core.database.UserRepository;
import com.catcher.core.domain.entity.User;
import com.catcher.core.domain.entity.enums.UserRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

import static com.catcher.core.domain.entity.enums.UserProvider.CATCHER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("local")
@SpringBootTest(classes = AppApplication.class)
@Transactional
class JwtTokenProviderTest {
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    UserRepository userRepository;

    @PersistenceContext
    EntityManager em;

    @DisplayName("Access Token 발급 시, 토큰 값은 비어있지 않아야 한다.")
    @Test
    void create_access_token() {
        //given
        User user = registerStubUser();
        Authentication authentication = createAuthentication(user.getUsername(), user.getPassword());

        //when
        String accessToken = jwtTokenProvider.createAccessToken(authentication);

        //then
        assertThat(accessToken).isNotEmpty();
    }

    @DisplayName("Refresh Token 발급 시, 토큰 값은 비어있지 않아야 한다.")
    @Test
    void create_refresh_token() {
        //given
        User user = registerStubUser();
        Authentication authentication = createAuthentication(user.getUsername(), user.getPassword());

        //when
        String accessToken = jwtTokenProvider.createRefreshToken(authentication);

        //then
        assertThat(accessToken).isNotEmpty();
    }

    @DisplayName("잘못된 아이디로 발급된 Token으로 Authentication 조회 시, 예외발생")
    @Test
    void wrong_username_get_authentication() {
        //given
        User user = registerStubUser();
        Authentication invalidId = createAuthentication(createRandomUUID(), user.getPassword());
        String accessToken = jwtTokenProvider.createAccessToken(invalidId);
        String refreshToken = jwtTokenProvider.createRefreshToken(invalidId);

        //when

        //then
        assertThatThrownBy(() -> jwtTokenProvider.getAuthentication(accessToken)).isInstanceOf(BaseException.class);
        assertThatThrownBy(() -> jwtTokenProvider.getAuthentication(refreshToken)).isInstanceOf(BaseException.class);
    }

    @DisplayName("잘못된 비밀번호로 발급된 Token으로 Authentication 조회 시, 예외발생")
    @Test
    void wrong_password_get_authentication() {
        //given
        User user = registerStubUser();
        Authentication invalidPassword = createAuthentication(user.getPassword(), createRandomUUID());
        String accessToken = jwtTokenProvider.createAccessToken(invalidPassword);
        String refreshToken = jwtTokenProvider.createRefreshToken(invalidPassword);

        //when

        //then
        assertThatThrownBy(() -> jwtTokenProvider.getAuthentication(accessToken)).isInstanceOf(BaseException.class);
        assertThatThrownBy(() -> jwtTokenProvider.getAuthentication(refreshToken)).isInstanceOf(BaseException.class);
    }

    @DisplayName("유효한 토큰으로 유효성 검증시 True 반환")
    @Test
    void verify_valid_token() {
        //given
        User user = registerStubUser();
        Authentication invalidPassword = createAuthentication(user.getPassword(), createRandomUUID());
        String accessToken = jwtTokenProvider.createAccessToken(invalidPassword);
        String refreshToken = jwtTokenProvider.createRefreshToken(invalidPassword);

        //when

        //then
        assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
    }

    @DisplayName("유효하지 않은 토큰으로 유효성 검증 시, 예외발생")
    @Test
    void verify_invalid_token() {
        //given
        User user = registerStubUser();
        Authentication invalidPassword = createAuthentication(user.getPassword(), createRandomUUID());
        String wrongAccessToken = jwtTokenProvider.createAccessToken(invalidPassword) + createRandomUUID().charAt(0);
        String wrongRefreshToken = jwtTokenProvider.createRefreshToken(invalidPassword) + createRandomUUID().charAt(0);

        //when

        //then
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(wrongAccessToken));
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(wrongRefreshToken));
    }

    private Authentication createAuthentication(String username, String password) {
        return new UsernamePasswordAuthenticationToken(
                username,
                password
        );
    }

    private User registerStubUser() {
        User user = User.builder()
                .username(createRandomUUID())
                .password(passwordEncoder.encode(createRandomUUID()))
                .phone(createRandomUUID())
                .email(createRandomUUID())
                .profileImageUrl(null)
                .introduceContent(null)
                .nickname(createRandomUUID())
                .userProvider(CATCHER)
                .role(UserRole.USER)
                .userAgeTerm(ZonedDateTime.now())
                .userServiceTerm(ZonedDateTime.now())
                .userPrivacyTerm(ZonedDateTime.now())
                .userLocationTerm(ZonedDateTime.now())
                .userMarketingTerm(ZonedDateTime.now())
                .build();

        userRepository.save(user);
        flushAndClearPersistence();
        return user;
    }

    private void flushAndClearPersistence() {
        em.flush();
        em.clear();
    }

    private String createRandomUUID() {
        return UUID.randomUUID().toString();
    }
}