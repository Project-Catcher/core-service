package com.catcher.core.service;

import com.catcher.app.AppApplication;
import com.catcher.common.exception.BaseException;
import com.catcher.core.database.DBManager;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.user.UserCreateRequest;
import com.catcher.testconfiguriation.EmbeddedRedisConfiguration;
import com.catcher.utils.KeyGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.catcher.utils.KeyGenerator.AuthType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest(classes = {AppApplication.class, EmbeddedRedisConfiguration.class})
@Transactional
class AuthServiceTest {
    @Autowired
    AuthService authService;
    @Autowired
    UserService userService;
    @PersistenceContext
    EntityManager em;
    @Autowired
    DBManager dbManager;

    @DisplayName("토큰 재발급시 기존 토큰값과 다른 값을 가진다.")
    @Test
    void valid_reissue_token() {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(createRandomUUID(), createRandomUUID(), createRandomUUID(), createRandomUUID());
        TokenDto preTokenDto = userService.signUpUser(userCreateRequest);

        //when
        TokenDto newTokenDto = authService.reissueRefreshToken(preTokenDto.getRefreshToken());

        //then
        assertThat(newTokenDto.getRefreshToken()).isNotEqualTo(preTokenDto.getRefreshToken());
        assertThat(newTokenDto.getAccessToken()).isNotEqualTo(preTokenDto.getAccessToken());
    }

    @DisplayName("유효하지 않은 refresh 토큰으로 재발급 시, 에러 반환")
    @Test
    void invalid_refresh_token_reissue_token() {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(createRandomUUID(), createRandomUUID(), createRandomUUID(), createRandomUUID());
        TokenDto tokenDto = userService.signUpUser(userCreateRequest);

        //when

        //then
        assertThatThrownBy(() -> authService.reissueRefreshToken(tokenDto.getRefreshToken() + "1"))
                .isInstanceOf(BaseException.class);
    }

    @DisplayName("리프레쉬 토큰 폐기 후, 재발급 시, 에러 반환")
    @Test
    void discard_refresh_token_then_reissue_token() {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(createRandomUUID(), createRandomUUID(), createRandomUUID(), createRandomUUID());
        TokenDto preTokenDto = userService.signUpUser(userCreateRequest);

        //when
        authService.discardRefreshToken(preTokenDto.getRefreshToken());

        //then
        assertThatThrownBy(() -> authService.reissueRefreshToken(preTokenDto.getRefreshToken()))
                .isInstanceOf(BaseException.class);
    }

    @DisplayName("액세스 토큰 폐기 시, 저장소에 블랙리스트 토큰 저장")
    @Test
    void discard_access_token() {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(createRandomUUID(), createRandomUUID(), createRandomUUID(), createRandomUUID());
        TokenDto tokenDto = userService.signUpUser(userCreateRequest);
        //when
        authService.discardAccessToken("Bearer " + tokenDto.getAccessToken());
        //then
        Optional<String> value = dbManager.getValue(KeyGenerator.generateKey(tokenDto.getAccessToken(), BLACK_LIST_ACCESS_TOKEN));
        assertThat(value).isPresent();
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

    private String createRandomUUID() {
        return UUID.randomUUID().toString();
    }
}