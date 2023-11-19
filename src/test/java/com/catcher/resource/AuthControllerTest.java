package com.catcher.resource;

import com.catcher.app.AppApplication;
import com.catcher.common.BaseResponseStatus;
import com.catcher.common.CatcherControllerAdvice;
import com.catcher.common.response.BaseResponse;
import com.catcher.core.dto.RefreshTokenDto;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.user.UserCreateRequest;
import com.catcher.core.service.UserService;
import com.catcher.testconfiguriation.EmbeddedRedisConfiguration;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

import static com.catcher.common.BaseResponseStatus.SUCCESS;
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

    RefreshTokenDto refreshTokenDto;

    @BeforeEach
    void beforeEach() throws Exception {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new CatcherControllerAdvice())
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .build();
        this.refreshTokenDto = createRefreshToken();
    }

    @DisplayName("정상 리프레쉬 토큰으로 요청 시, 새로운 토큰 발행")
    @Test
    void reissue_token() throws Exception {
        //given

        //when
        ResultActions resultActions = mockMvc.perform(post("/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenDto))
        );

        //then
        resultActions.andExpect(status().isOk());
        TokenDto responseTokenDto = getResponseObject(resultActions.andReturn().getResponse(), TokenDto.class);

        assertThat(responseTokenDto.getAccessToken()).isNotNull();
        assertThat(responseTokenDto.getRefreshToken()).isNotEqualTo(refreshTokenDto.getRefreshToken());
    }

    @DisplayName("정상 리프레쉬 토큰으로 폐기 시, 정상 응답")
    @Test
    void discard_token() throws Exception {
        //given

        //when
        ResultActions resultActions = mockMvc.perform(post("/auth/discard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenDto))
        );

        //then
        resultActions.andExpect(status().isOk());
        BaseResponse baseResponse = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), BaseResponse.class);

        assertThat(baseResponse.getMessage()).isEqualTo(SUCCESS.getMessage());
        assertThat(baseResponse.getCode()).isEqualTo(SUCCESS.getCode());
        assertThat(baseResponse.getIsSuccess()).isEqualTo(true);
    }

    @DisplayName("비정상 리프레쉬 토큰으로 폐기 시, 예외 응답")
    @Test
    void invalid_discard_token() throws Exception {
        //given
        RefreshTokenDto invalidRefreshToken = new RefreshTokenDto(refreshTokenDto.getRefreshToken() + "1");

        //when
        MvcResult mvcResult = mockMvc.perform(post("/auth/discard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRefreshToken))
        ).andReturn();
        BaseResponse baseResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), BaseResponse.class);

        //then
        assertThat(baseResponse.getIsSuccess()).isFalse();
        assertThat(baseResponse.getResult()).isNull();
        assertThat(baseResponse.getCode()).isEqualTo(BaseResponseStatus.INVALID_JWT.getCode());
        assertThat(baseResponse.getMessage()).isEqualTo(BaseResponseStatus.INVALID_JWT.getMessage());
    }

    @DisplayName("비정상 리프레쉬 토큰으로 폐기 시, 예외 응답")
    @Test
    void invalid_reissue_token() throws Exception {
        //given
        RefreshTokenDto invalidRefreshToken = new RefreshTokenDto(refreshTokenDto.getRefreshToken() + "1");

        //when
        MvcResult mvcResult = mockMvc.perform(post("/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRefreshToken))
        ).andReturn();
        BaseResponse baseResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), BaseResponse.class);

        //then
        assertThat(baseResponse.getIsSuccess()).isFalse();
        assertThat(baseResponse.getResult()).isNull();
        assertThat(baseResponse.getCode()).isEqualTo(BaseResponseStatus.INVALID_JWT.getCode());
        assertThat(baseResponse.getMessage()).isEqualTo(BaseResponseStatus.INVALID_JWT.getMessage());
    }

    private <T> T getResponseObject(MockHttpServletResponse response, Class<T> type) throws IOException {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, type);
        BaseResponse<T> result = objectMapper.readValue(response.getContentAsString(), javaType);
        return result.getResult();
    }

    private RefreshTokenDto createRefreshToken() {
        String refreshToken = userService.signUpUser(userCreateRequest()).getRefreshToken();
        flushAndClearPersistence();
        return new RefreshTokenDto(refreshToken);
    }

    private UserCreateRequest userCreateRequest() {
        return UserCreateRequest.builder()
                .nickname(createRandomUUID())
                .ageTerm(ZonedDateTime.now())
                .locationTerm(ZonedDateTime.now())
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