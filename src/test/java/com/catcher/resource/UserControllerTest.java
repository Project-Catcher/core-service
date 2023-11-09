package com.catcher.resource;

import com.catcher.app.AppApplication;
import com.catcher.common.response.BaseResponse;
import com.catcher.core.database.UserRepository;
import com.catcher.core.domain.entity.User;
import com.catcher.core.domain.entity.enums.UserRole;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.user.UserCreateRequest;
import com.catcher.core.dto.user.UserLoginRequest;
import com.catcher.testconfiguriation.EmbeddedRedisConfiguration;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.UUID;

import static com.catcher.core.domain.entity.enums.UserProvider.CATCHER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles("test")
@SpringBootTest(classes = {AppApplication.class, EmbeddedRedisConfiguration.class})
@Transactional
class UserControllerTest {
    @PersistenceContext
    EntityManager em;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserController userController;

    @Autowired
    ObjectMapper objectMapper;

    User user;

    String rawPassword;

    MockMvc mockMvc;

    @BeforeEach
    void beforeEach() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .build();
        user = userRepository.save(createUser());
        flushAndClearPersistence();
    }

    @DisplayName("동일한 아이디로 회원가입시 예외발생")
    @Test
    void invalid_username_signup() throws Exception {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(
                createRandomUUID(),
                createRandomUUID(),
                createRandomUUID(),
                user.getUsername()
        );

        //when

        //then
        assertThatThrownBy(() ->
                mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateRequest))
                )).isInstanceOf(ServletException.class);
    }

    @DisplayName("동일한 이메일로 회원가입시 예외발생")
    @Test
    void invalid_email_signup() throws Exception {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(
                createRandomUUID(),
                createRandomUUID(),
                user.getEmail(),
                createRandomUUID()
        );

        //when

        //then
        assertThatThrownBy(() ->
                mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateRequest))
                )).isInstanceOf(ServletException.class);
    }

    @DisplayName("동일한 핸드폰으로 회원가입시 예외발생")
    @Test
    void invalid_phone_signup() throws Exception {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(
                createRandomUUID(),
                user.getPhone(),
                createRandomUUID(),
                createRandomUUID()
        );

        //when

        //then
        assertThatThrownBy(() ->
                mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateRequest))
                )).isInstanceOf(ServletException.class);
    }

    @DisplayName("동일한 닉네임 회원가입시 예외발생")
    @Test
    void invalid_nickname_signup() throws Exception {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(
                user.getNickname(),
                createRandomUUID(),
                createRandomUUID(),
                createRandomUUID()
        );

        //when

        //then
        assertThatThrownBy(() ->
                mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateRequest))
                )).isInstanceOf(ServletException.class);
    }

    @DisplayName("정상 회원가입 시 토큰 발행")
    @Test
    void valid_signup() throws Exception{
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(
                createRandomUUID(),
                createRandomUUID(),
                createRandomUUID(),
                createRandomUUID()
        );

        //when
        ResultActions resultActions = mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateRequest))
        );

        //then
        TokenDto tokenDto = getResponseObject(resultActions.andReturn().getResponse(), TokenDto.class);
        assertThat(tokenDto.getRefreshToken()).isNotNull();
        assertThat(tokenDto.getAccessToken()).isNotNull();
    }

    @DisplayName("유효하지 않은 아이디로 로그인시 예외 발생")
    @Test
    void invalid_username_login() throws Exception {
        //given
        UserLoginRequest userLoginRequest = new UserLoginRequest(createRandomUUID(), rawPassword);

        //when

        //then
        assertThatThrownBy(() ->
                mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLoginRequest))
                )).isInstanceOf(ServletException.class);
    }

    @DisplayName("유효하지 않은 비밀번호로 로그인시 예외 발생")
    @Test
    void invalid_password_login() throws Exception {
        //given
        UserLoginRequest userLoginRequest = new UserLoginRequest(user.getUsername(), createRandomUUID());

        //when

        //then
        assertThatThrownBy(() ->
                mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLoginRequest))
                )).isInstanceOf(ServletException.class);
    }

    @DisplayName("올바른 아이디, 비밀번호로 로그인시 토큰 반환")
    @Test
    void valid_login() throws Exception {
        //given
        UserLoginRequest userLoginRequest = new UserLoginRequest(user.getUsername(), rawPassword);

        //when
        ResultActions resultActions = mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLoginRequest))
        );

        //then
        TokenDto tokenDto = getResponseObject(resultActions.andReturn().getResponse(), TokenDto.class);
        assertThat(tokenDto.getRefreshToken()).isNotNull();
        assertThat(tokenDto.getAccessToken()).isNotNull();
    }

    private <T> T getResponseObject(MockHttpServletResponse response, Class<T> type) throws IOException {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, type);
        BaseResponse<T> result = objectMapper.readValue(response.getContentAsString(), javaType);
        return result.getResult();
    }

    private UserCreateRequest userCreateRequest(String nickname, String phone, String email, String username) {
        return UserCreateRequest.builder()
                .nickname(nickname)
                .ageTerm(ZonedDateTime.now())
                .locationTerm(ZonedDateTime.now())
                .serviceTerm(ZonedDateTime.now())
                .marketingTerm(ZonedDateTime.now())
                .privacyTerm(ZonedDateTime.now())
                .phone(phone)
                .email(email)
                .username(username)
                .password(createRandomUUID())
                .build();
    }

    private User createUser() {
        this.rawPassword = createRandomUUID();
        return User.builder()
                .username(createRandomUUID())
                .password(passwordEncoder.encode(rawPassword))
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
    }

    private void flushAndClearPersistence() {
        em.flush();
        em.clear();
    }

    private String createRandomUUID() {
        return UUID.randomUUID().toString();
    }
}