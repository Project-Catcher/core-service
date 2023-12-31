package com.catcher.resource;

import com.catcher.app.AppApplication;
import com.catcher.common.BaseResponseStatus;
import com.catcher.common.CatcherControllerAdvice;
import com.catcher.common.GlobalExceptionHandlerFilter;
import com.catcher.common.response.CommonResponse;
import com.catcher.config.JwtFilter;
import com.catcher.config.JwtTokenProvider;
import com.catcher.core.database.DBManager;
import com.catcher.core.database.UserRepository;
import com.catcher.core.domain.entity.User;
import com.catcher.core.domain.entity.enums.UserGender;
import com.catcher.core.domain.entity.enums.UserRole;
import com.catcher.core.dto.user.UserCreateRequest;
import com.catcher.core.dto.user.UserInfoResponse;
import com.catcher.core.dto.user.UserLoginRequest;
import com.catcher.infrastructure.external.service.S3UploadService;
import com.catcher.resource.request.PromotionRequest;
import com.catcher.resource.request.UserInfoEditRequest;
import com.catcher.resource.response.UserDetailsResponse;
import com.catcher.testconfiguriation.EmbeddedRedisConfiguration;
import com.catcher.testconfiguriation.WithCustomMockUser;
import com.catcher.utils.KeyGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import static com.catcher.common.BaseResponseStatus.SUCCESS;
import static com.catcher.common.BaseResponseStatus.USERS_NOT_LOGIN;
import static com.catcher.core.domain.entity.enums.UserProvider.CATCHER;
import static com.catcher.utils.JwtUtils.REFRESH_TOKEN_NAME;
import static com.catcher.utils.KeyGenerator.AuthType.BLACK_LIST_ACCESS_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @MockBean
    S3UploadService mockS3UploadService;

    User user;

    String rawPassword;

    MockMvc mockMvc;

    @BeforeEach
    void beforeEach() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new CatcherControllerAdvice())
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .addFilter(new GlobalExceptionHandlerFilter(objectMapper))
                .addFilter(new JwtFilter(jwtTokenProvider, dbManager))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
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
    void valid_signup() throws Exception {
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
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .content(objectMapper.writeValueAsString(userLoginRequest))
        ).andExpect(status().isOk());

        //then
        assertThat(dbManager.getValue(KeyGenerator.generateKey(accessToken, BLACK_LIST_ACCESS_TOKEN))).isPresent();
    }

    @DisplayName("비정상 토큰으로 로그아웃 시, 400 응답")
    @Test
    void invalid_logout() throws Exception {
        UserLoginRequest userLoginRequest = new UserLoginRequest(user.getUsername(), rawPassword);
        String invalidAccessToken = "gadsklgasg.fadsfklalsfjks.fadsklfsa";

        //when
        ResultActions logoutResult = mockMvc.perform(delete("/users/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidAccessToken)
                .content(objectMapper.writeValueAsString(userLoginRequest))
        ).andExpect(status().isBadRequest());

        //then
        assertThat(dbManager.getValue(KeyGenerator.generateKey(invalidAccessToken, BLACK_LIST_ACCESS_TOKEN))).isEmpty();
    }

    @DisplayName("로그인 한 유저의 정보를 정상적으로 불러옴")
    @Test
    @WithCustomMockUser(username = "test", role = UserRole.USER)
    void valid_getMyInfo() throws Exception {
        //given

        //when
        MvcResult mvcResult = mockMvc.perform(get("/users/info")
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();

        CommonResponse<UserInfoResponse> commonResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
        });

        //then
        assertThat(commonResponse.isSuccess()).isTrue();
        assertThat(commonResponse.getResult().getUsername()).isEqualTo("test");
    }

    @DisplayName("로그인하지 않은 회원이 회원탈퇴 시 예외 반환")
    @Test
    void invalid_sign_out() throws Exception {
        // given

        // when
        MvcResult mvcResult = mockMvc.perform(delete("/users/sign-out")
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn();
        CommonResponse commonResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CommonResponse.class);

        // then
        assertThat(commonResponse.isSuccess()).isFalse();
        assertThat(commonResponse.getCode()).isEqualTo(USERS_NOT_LOGIN.getCode());
        assertThat(commonResponse.getResult()).isEqualTo(USERS_NOT_LOGIN.getMessage());
    }

    @DisplayName("탈퇴한 유저는 로그인에 실패해야 한다.")
    @Test
    void sign_out_user_login() throws Exception {
        //given
        UserLoginRequest userLoginRequest = new UserLoginRequest(user.getUsername(), rawPassword);
        //로그인 요청
        ResultActions loginAction = mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLoginRequest))

        );
        String accessToken = (String) objectMapper.readValue(loginAction.andReturn().getResponse().getContentAsString(), CommonResponse.class).getResult();
        // 회원 탈퇴
        ResultActions signOutAction = mockMvc.perform(delete("/users/sign-out")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLoginRequest))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        );

        //when
        //로그인 요청
        ResultActions reLoginAction = mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLoginRequest))

        );

        //then
        CommonResponse commonResponse = objectMapper.readValue(reLoginAction.andReturn().getResponse().getContentAsString(), CommonResponse.class);
        assertThat(commonResponse.isSuccess()).isFalse();
    }

    @DisplayName("핸드폰 수신정보 동의 변경 요청 시 값이 정상적으로 변경되어야 한다.")
    @Test
    void email_promotion_request() throws Exception {
        //given
        UserLoginRequest userLoginRequest = new UserLoginRequest(user.getUsername(), rawPassword);
        String returnString = mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLoginRequest))
        ).andReturn().getResponse().getContentAsString();
        String accessToken = (String) objectMapper.readValue(returnString, CommonResponse.class).getResult();

        for (int i = 0; i < 2; i++) {
            boolean beforeCurrentPhonePromotionOn = null != userRepository.findById(user.getId())
                    .orElseThrow()
                    .getPhoneMarketingTerm();

            //when
            mockMvc.perform(post("/users/promotion/phone")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .content(objectMapper.writeValueAsString(new PromotionRequest(!beforeCurrentPhonePromotionOn)))
            ).andExpect(status().isOk());
            flushAndClearPersistence();

            //then
            User currentUser = userRepository.findById(user.getId()).orElseThrow();
            if (beforeCurrentPhonePromotionOn) {
                assertThat(currentUser.getPhoneMarketingTerm()).isNull();
            } else {
                assertThat(currentUser.getPhoneMarketingTerm()).isNotNull();
            }
        }
    }

    @DisplayName("이메일 수신정보 동의 변경 요청 시 값이 정상적으로 변경되어야 한다.")
    @Test
    void phone_promotion_request() throws Exception {
        //given
        UserLoginRequest userLoginRequest = new UserLoginRequest(user.getUsername(), rawPassword);
        String returnString = mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLoginRequest))
        ).andReturn().getResponse().getContentAsString();
        String accessToken = (String) objectMapper.readValue(returnString, CommonResponse.class).getResult();

        for (int i = 0; i < 2; i++) {
            boolean beforeCurrentEmailPromotionOn = null != userRepository.findById(user.getId())
                    .orElseThrow()
                    .getEmailMarketingTerm();


            //when
            mockMvc.perform(post("/users/promotion/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .content(objectMapper.writeValueAsString(new PromotionRequest(!beforeCurrentEmailPromotionOn)))
            ).andExpect(status().isOk());
            flushAndClearPersistence();

            //then
            User currentUser = userRepository.findById(user.getId()).orElseThrow();
            if (beforeCurrentEmailPromotionOn) {
                assertThat(currentUser.getEmailMarketingTerm()).isNull();
            } else {
                assertThat(currentUser.getEmailMarketingTerm()).isNotNull();
            }
        }
    }

    @DisplayName("세부 정보 요청 시, 응답 값이 현재 유저 정보와 일치해야한다.")
    @Test
    void request_user_info_details() throws Exception {
        //given
        UserLoginRequest userLoginRequest = new UserLoginRequest(user.getUsername(), rawPassword);
        String returnString = mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLoginRequest))
        ).andReturn().getResponse().getContentAsString();
        String accessToken = (String) objectMapper.readValue(returnString, CommonResponse.class).getResult();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        //when
        MvcResult mvcResult = mockMvc.perform(get("/users/info/details")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        ).andReturn();
        CommonResponse<UserDetailsResponse> commonResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
        });
        UserDetailsResponse result = commonResponse.getResult();

        // then
        assertThat(commonResponse.isSuccess()).isTrue();
        assertThat(commonResponse.getCode()).isEqualTo(SUCCESS.getCode());
        assertThat(dateFormat.format(user.getBirthDate())).isEqualTo(dateFormat.format(result.getBirth()));
        assertThat(user.getNickname()).isEqualTo(result.getNickname());
        assertThat(user.getUserGender()).isEqualTo(result.getUserGender());
        assertThat(user.getPhone()).isEqualTo(result.getPhone());
        assertThat(user.getEmail()).isEqualTo(result.getEmail());
    }

    @DisplayName("유저 정보와 썸네일 변경 시 정상적으로 변경되어야 한다.")
    @Test
    void edit_request_user() throws Exception {
        //given
        UserLoginRequest userLoginRequest = new UserLoginRequest(user.getUsername(), rawPassword);
        String returnString = mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLoginRequest))
        ).andReturn().getResponse().getContentAsString();
        String accessToken = (String) objectMapper.readValue(returnString, CommonResponse.class).getResult();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        for (UserGender gender : UserGender.values()) {
            MockMultipartFile mockMultipartFile = new MockMultipartFile(
                    "profile_file",
                    createRandomUUID().getBytes()
            );
            UserInfoEditRequest userInfoEditRequest = new UserInfoEditRequest(
                    createRandomUUID(),
                    Timestamp.valueOf(LocalDateTime.now().minusDays(1)),
                    gender
            );
            String editRequestJson = "{" +
                    "\"nickname\":\"" + userInfoEditRequest.getNickname() + "\"," +
                    "\"birth\":\"" + dateFormat.format(userInfoEditRequest.getBirth()) + "\"," +
                    "\"gender\":\"" + userInfoEditRequest.getGender().name() + "\"" +
                    "}";
            MockMultipartFile requestJson = new MockMultipartFile("userInfoEditRequest", "test", "application/json", editRequestJson.getBytes(StandardCharsets.UTF_8));

            //when
            String fileUri = createRandomUUID();
            when(mockS3UploadService.uploadFile(mockMultipartFile)).thenReturn(fileUri);

            mockMvc.perform(
                    MockMvcRequestBuilders.multipart("/users/info/edit")
                            .file(mockMultipartFile)
                            .file(requestJson)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );
            flushAndClearPersistence();

            //then
            User user = userRepository.findByNickname(userInfoEditRequest.getNickname())
                    .orElseThrow();

            assertThat(user.getUserGender()).isEqualTo(userInfoEditRequest.getGender());
            assertThat(dateFormat.format(user.getBirthDate())).isEqualTo(dateFormat.format(userInfoEditRequest.getBirth()));
            assertThat(user.getNickname()).isEqualTo(userInfoEditRequest.getNickname());
            assertThat(user.getProfileImageUrl()).isEqualTo(fileUri);
        }
    }

    private UserCreateRequest userCreateRequest(String username, String nickname, String phone, String email) {
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
                .userAgeTerm(LocalDateTime.now())
                .userServiceTerm(LocalDateTime.now())
                .userPrivacyTerm(LocalDateTime.now())
                .emailMarketingTerm(LocalDateTime.now())
                .phoneMarketingTerm(LocalDateTime.now())
                .birthDate(new Date())
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