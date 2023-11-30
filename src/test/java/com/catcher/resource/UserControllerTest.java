package com.catcher.resource;

import com.catcher.app.AppApplication;
import com.catcher.common.BaseResponseStatus;
import com.catcher.common.CatcherControllerAdvice;
import com.catcher.common.response.CommonResponse;
import com.catcher.core.database.DBManager;
import com.catcher.core.database.UserRepository;
import com.catcher.core.domain.entity.User;
import com.catcher.core.domain.entity.enums.UserRole;
import com.catcher.core.dto.user.UserCreateRequest;
import com.catcher.core.dto.user.UserLoginRequest;
import com.catcher.testconfiguriation.EmbeddedRedisConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.ZonedDateTime;
import java.util.UUID;

import static com.catcher.core.domain.entity.enums.UserProvider.CATCHER;
import static com.catcher.utils.JwtUtils.REFRESH_TOKEN_NAME;
import static com.catcher.utils.JwtUtils.generateBlackListToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    @Autowired
    DBManager dbManager;

    User user;

    String rawPassword;

    MockMvc mockMvc;

    @BeforeEach
    void beforeEach() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new CatcherControllerAdvice())
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .build();
        user = userRepository.save(createUser());
        flushAndClearPersistence();
    }

    @DisplayName("동일한 아이디로 회원가입시 예외 응답")
    @Test
    void invalid_username_signup() throws Exception {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(
                user.getUsername(),
                createRandomUUID(),
                createRandomUUID(),
                createRandomUUID()
        );

        //when
        MvcResult mvcResult = mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateRequest))
        ).andReturn();

        CommonResponse commonResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CommonResponse.class);

        //then
        assertThat(commonResponse.isSuccess()).isFalse();
        assertThat(commonResponse.getCode()).isEqualTo(BaseResponseStatus.USERS_DUPLICATED_USER_NAME.getCode());
        assertThat(commonResponse.getResult()).isEqualTo(BaseResponseStatus.USERS_DUPLICATED_USER_NAME.getMessage());
    }

    @DisplayName("동일한 이메일로 회원가입시 예외 응답")
    @Test
    void invalid_email_signup() throws Exception {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(
                createRandomUUID(),
                createRandomUUID(),
                createRandomUUID(),
                user.getEmail()
        );

        //when
        MvcResult mvcResult = mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateRequest))
        ).andReturn();

        CommonResponse commonResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CommonResponse.class);

        //then
        assertThat(commonResponse.isSuccess()).isFalse();
        assertThat(commonResponse.getCode()).isEqualTo(BaseResponseStatus.USERS_DUPLICATED_USER_EMAIL.getCode());
        assertThat(commonResponse.getResult()).isEqualTo(BaseResponseStatus.USERS_DUPLICATED_USER_EMAIL.getMessage());
    }

    @DisplayName("동일한 핸드폰으로 회원가입시 예외 응답")
    @Test
    void invalid_phone_signup() throws Exception {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(
                createRandomUUID(),
                createRandomUUID(),
                user.getPhone(),
                createRandomUUID()
        );

        //when
        MvcResult mvcResult = mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateRequest))
        ).andReturn();

        CommonResponse commonResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CommonResponse.class);

        //then
        assertThat(commonResponse.isSuccess()).isFalse();
        assertThat(commonResponse.getCode()).isEqualTo(BaseResponseStatus.USERS_DUPLICATED_PHONE.getCode());
        assertThat(commonResponse.getResult()).isEqualTo(BaseResponseStatus.USERS_DUPLICATED_PHONE.getMessage());
    }

    @DisplayName("동일한 닉네임 회원가입시 예외 응답")
    @Test
    void invalid_nickname_signup() throws Exception {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(
                createRandomUUID(),
                user.getNickname(),
                createRandomUUID(),
                createRandomUUID()
        );

        //when
        MvcResult mvcResult = mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateRequest))
        ).andReturn();

        CommonResponse commonResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CommonResponse.class);

        //then
        assertThat(commonResponse.isSuccess()).isFalse();
        assertThat(commonResponse.getCode()).isEqualTo(BaseResponseStatus.USERS_DUPLICATED_NICKNAME.getCode());
        assertThat(commonResponse.getResult()).isEqualTo(BaseResponseStatus.USERS_DUPLICATED_NICKNAME.getMessage());
    }

    @DisplayName("동일한 아이디와 닉네임으로 회원가입시 중복 아이디 예외 응답")
    @Test
    void invalid_username_nickname_signup() throws Exception {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(
                user.getUsername(),
                user.getNickname(),
                createRandomUUID(),
                createRandomUUID()
        );

        //when
        MvcResult mvcResult = mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateRequest))
        ).andReturn();

        CommonResponse commonResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CommonResponse.class);

        //then
        assertThat(commonResponse.isSuccess()).isFalse();
        assertThat(commonResponse.getCode()).isEqualTo(BaseResponseStatus.USERS_DUPLICATED_USER_NAME.getCode());
        assertThat(commonResponse.getResult()).isEqualTo(BaseResponseStatus.USERS_DUPLICATED_USER_NAME.getMessage());
    }

    @DisplayName("동일한 닉네임과 핸드폰으로 회원가입시 중복 닉네임 예외 응답")
    @Test
    void invalid_nickname_phone_signup() throws Exception {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(
                createRandomUUID(),
                user.getNickname(),
                user.getPhone(),
                createRandomUUID()
        );

        //when
        MvcResult mvcResult = mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateRequest))
        ).andReturn();

        CommonResponse commonResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CommonResponse.class);

        //then
        assertThat(commonResponse.isSuccess()).isFalse();
        assertThat(commonResponse.getCode()).isEqualTo(BaseResponseStatus.USERS_DUPLICATED_NICKNAME.getCode());
        assertThat(commonResponse.getResult()).isEqualTo(BaseResponseStatus.USERS_DUPLICATED_NICKNAME.getMessage());
    }

    @DisplayName("동일한 핸드폰과 이메일로 회원가입시 중복 핸드폰 예외 응답")
    @Test
    void invalid_phone_email_signup() throws Exception {
        //given
        UserCreateRequest userCreateRequest = userCreateRequest(
                createRandomUUID(),
                createRandomUUID(),
                user.getPhone(),
                user.getEmail()
        );

        //when
        MvcResult mvcResult = mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateRequest))
        ).andReturn();

        CommonResponse commonResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CommonResponse.class);

        //then
        assertThat(commonResponse.isSuccess()).isFalse();
        assertThat(commonResponse.getCode()).isEqualTo(BaseResponseStatus.USERS_DUPLICATED_PHONE.getCode());
        assertThat(commonResponse.getResult()).isEqualTo(BaseResponseStatus.USERS_DUPLICATED_PHONE.getMessage());
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
        CommonResponse commonResponse = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), CommonResponse.class);

        //then
        assertThat(commonResponse.isSuccess()).isTrue();
        assertThat(commonResponse.getResult()).isNotNull();
        assertThat(resultActions.andReturn().getResponse().getCookie(REFRESH_TOKEN_NAME)).isNotNull();
    }

    @DisplayName("유효하지 않은 아이디로 로그인시 예외 응답")
    @Test
    void invalid_username_login() throws Exception {
        //given
        UserLoginRequest userLoginRequest = new UserLoginRequest(createRandomUUID(), rawPassword);

        //when
        MvcResult mvcResult = mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLoginRequest))
        ).andReturn();
        CommonResponse commonResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CommonResponse.class);

        //then
        assertThat(commonResponse.isSuccess()).isFalse();
        assertThat(commonResponse.getCode()).isEqualTo(BaseResponseStatus.INVALID_USER_NAME.getCode());
        assertThat(commonResponse.getResult()).isEqualTo(BaseResponseStatus.INVALID_USER_NAME.getMessage());
    }

    @DisplayName("유효하지 않은 비밀번호로 로그인시 예외 응답")
    @Test
    void invalid_password_login() throws Exception {
        //given
        UserLoginRequest userLoginRequest = new UserLoginRequest(user.getUsername(), createRandomUUID());

        //when
        MvcResult mvcResult = mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLoginRequest))
        ).andReturn();
        CommonResponse commonResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CommonResponse.class);

        //then
        assertThat(commonResponse.isSuccess()).isFalse();
        assertThat(commonResponse.getCode()).isEqualTo(BaseResponseStatus.INVALID_USER_PW.getCode());
        assertThat(commonResponse.getResult()).isEqualTo(BaseResponseStatus.INVALID_USER_PW.getMessage());
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
        CommonResponse commonResponse = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), CommonResponse.class);

        //then

        assertThat(commonResponse.isSuccess()).isTrue();
        assertThat(commonResponse.getResult()).isNotNull();
        assertThat(resultActions.andReturn().getResponse().getCookie(REFRESH_TOKEN_NAME)).isNotNull();
    }

    @DisplayName("정상 토큰으로 로그아웃 시, 블랙리스트 토큰 저장")
    @Test
    void valid_logout() throws Exception {
        UserLoginRequest userLoginRequest = new UserLoginRequest(user.getUsername(), rawPassword);
        //when
        ResultActions resultActions = mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLoginRequest))
        );
        String accessToken = (String) objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), CommonResponse.class).getResult();
        ResultActions logoutResult = mockMvc.perform(delete("/users/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .content(objectMapper.writeValueAsString(userLoginRequest))
        ).andExpect(status().isOk());
        //then
        assertThat(dbManager.getValue(generateBlackListToken(accessToken))).isPresent();
    }
    
    @DisplayName("비정상 토큰으로 로그아웃 시, 200 정상 응답")
    @Test
    void invalid_logout() throws Exception {
        UserLoginRequest userLoginRequest = new UserLoginRequest(user.getUsername(), rawPassword);
        String invalidAccessToken = "gadsklgasg.fadsfklalsfjks.fadsklfsa";
        //when
        ResultActions logoutResult = mockMvc.perform(delete("/users/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, invalidAccessToken)
                .content(objectMapper.writeValueAsString(userLoginRequest))
        ).andExpect(status().isOk());
        //then
        assertThat(dbManager.getValue(generateBlackListToken(invalidAccessToken))).isEmpty();
    }

    private UserCreateRequest userCreateRequest(String username, String nickname, String phone, String email) {
        return UserCreateRequest.builder()
                .nickname(nickname)
                .ageTerm(ZonedDateTime.now())
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
                .userRole(UserRole.USER)
                .userAgeTerm(ZonedDateTime.now())
                .userServiceTerm(ZonedDateTime.now())
                .userPrivacyTerm(ZonedDateTime.now())
                .emailMarketingTerm(ZonedDateTime.now())
                .phoneMarketingTerm(ZonedDateTime.now())
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