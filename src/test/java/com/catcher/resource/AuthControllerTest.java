package com.catcher.resource;

import com.catcher.app.AppApplication;
import com.catcher.common.BaseResponseStatus;
import com.catcher.common.CatcherControllerAdvice;
import com.catcher.common.response.CommonResponse;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.user.UserCreateRequest;
import com.catcher.core.service.UserService;
import com.catcher.testconfiguriation.EmbeddedRedisConfiguration;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.UUID;

import static com.catcher.utils.JwtUtils.REFRESH_TOKEN_EXPIRATION_MILLIS;
import static com.catcher.utils.JwtUtils.REFRESH_TOKEN_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("test")
@SpringBootTest(classes = {AppApplication.class, EmbeddedRedisConfiguration.class})
@Transactional
class AuthControllerTest {

    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AuthController authController;

    @Autowired
    UserService userService;

    @PersistenceContext
    EntityManager em;

    String previousRefreshToken;
    String previousAccessToken;

    @BeforeEach
    void beforeEach() throws Exception {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new CatcherControllerAdvice())
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .build();
        TokenDto tokenDto = createRefreshToken();
        this.previousRefreshToken = tokenDto.getRefreshToken();
        this.previousAccessToken = tokenDto.getAccessToken();
    }

    @DisplayName("정상 리프레쉬 토큰으로 새로운 토큰 요청 시, 새로운 토큰 발행되어야 한다.")
    @Test
    void reissue_token() throws Exception {
        //given
        Cookie cookie = generateCookie(REFRESH_TOKEN_NAME, previousRefreshToken);

        //when
        ResultActions resultActions = mockMvc.perform(post("/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(previousRefreshToken))
                .cookie(cookie)
        ).andExpect(status().isOk());
        CommonResponse commonResponse = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), CommonResponse.class);

        //then
        assertThat(commonResponse.isSuccess()).isTrue();
        assertThat(commonResponse.getResult()).isNotNull();
        assertThat(commonResponse.getResult()).isNotEqualTo(previousAccessToken);
        assertThat(resultActions.andReturn().getResponse().getCookie(REFRESH_TOKEN_NAME)).isNotNull();
        assertThat(resultActions.andReturn().getResponse().getCookie(REFRESH_TOKEN_NAME).getValue()).isNotEqualTo(previousRefreshToken);
    }

    @DisplayName("비정상 리프레쉬 토큰으로 새로운 토큰 요청시, 예외 응답")
    @Test
    void invalid_reissue_token() throws Exception {
        //given
        Cookie cookie = generateCookie(REFRESH_TOKEN_NAME, previousRefreshToken + "1");

        //when
        MvcResult mvcResult = mockMvc.perform(post("/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie)
        ).andReturn();

        CommonResponse commonResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CommonResponse.class);

        //then
        assertThat(commonResponse.isSuccess()).isFalse();
        assertThat(commonResponse.getCode()).isEqualTo(BaseResponseStatus.INVALID_JWT.getCode());
        assertThat(commonResponse.getResult()).isEqualTo(BaseResponseStatus.INVALID_JWT.getMessage());
    }

    private Cookie generateCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setMaxAge((int) REFRESH_TOKEN_EXPIRATION_MILLIS);
        cookie.setPath("/");
        return  cookie;
    }

    private <T> T getResponseObject(MockHttpServletResponse response, Class<T> type) throws IOException {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(CommonResponse.class, type);
        CommonResponse<T> result = objectMapper.readValue(response.getContentAsString(), javaType);
        return result.getResult();
    }

    private TokenDto createRefreshToken() {
        TokenDto tokenDto = userService.signUpUser(userCreateRequest());
        flushAndClearPersistence();
        return tokenDto;
    }

    private UserCreateRequest userCreateRequest() {
        return UserCreateRequest.builder()
                .nickname(createRandomUUID())
                .ageTerm(ZonedDateTime.now())
                .serviceTerm(ZonedDateTime.now())
                .marketingTerm(ZonedDateTime.now())
                .privacyTerm(ZonedDateTime.now())
                .phone(createRandomUUID())
                .email(createRandomUUID())
                .username(createRandomUUID())
                .password(createRandomUUID())
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