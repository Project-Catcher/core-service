package com.catcher.resource;

import cn.apiclub.captcha.Captcha;
import com.catcher.common.response.CommonResponse;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.user.UserCreateRequest;
import com.catcher.core.dto.user.UserLoginRequest;
import com.catcher.core.service.CaptchaService;
import com.catcher.core.service.EmailService;
import com.catcher.core.service.UserService;
import com.catcher.core.service.authcode.AuthCodeServiceBase;
import com.catcher.resource.request.AuthCodeSendRequest;
import com.catcher.resource.request.AuthCodeVerifyRequest;
import com.catcher.resource.request.CaptchaGenerateRequest;
import com.catcher.resource.request.CaptchaValidateRequest;
import com.catcher.resource.resolver.annotation.AuthCodeSendInject;
import com.catcher.resource.resolver.annotation.AuthCodeVerifyInject;
import com.catcher.resource.response.AuthCodeVerifyResponse;
import com.catcher.resource.response.CaptchaValidateResponse;
import com.catcher.resource.request.PWChangeRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import static com.catcher.common.response.CommonResponse.success;
import static com.catcher.config.JwtTokenProvider.setRefreshCookie;
import static com.catcher.utils.HttpServletUtils.deleteCookie;
import static com.catcher.utils.JwtUtils.REFRESH_TOKEN_NAME;
import static com.catcher.utils.KeyGenerator.AuthType;
import static com.catcher.utils.KeyGenerator.AuthType.FIND_ID;
import static com.catcher.utils.KeyGenerator.AuthType.FIND_PASSWORD;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    public final static String FIND_ID_URL = "/find-id";
    public final static String FIND_PW_URL = "/find-pw";

    private final UserService userService;
    private final EmailService emailService;
    private final CaptchaService captchaService;
    private final List<AuthCodeServiceBase> authCodeServices;

    @Operation(summary = "회원 가입")
    @PostMapping("/signup")
    public CommonResponse<String> signUp(@Valid @RequestBody UserCreateRequest userCreateRequest, HttpServletResponse response) {
        TokenDto tokenDto = userService.signUpUser(userCreateRequest);
        setRefreshCookie(response, tokenDto.getRefreshToken());
        return success(tokenDto.getAccessToken());
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public CommonResponse<String> login(@Valid @RequestBody UserLoginRequest userLoginReqDto, HttpServletResponse response) {
        TokenDto tokenDto = userService.login(userLoginReqDto);
        setRefreshCookie(response, tokenDto.getRefreshToken());
        return success(tokenDto.getAccessToken());
    }

    @Operation(summary = "로그아웃")
    @DeleteMapping("/logout")
    public CommonResponse<Void> logout(HttpServletRequest request,
                                       HttpServletResponse response,
                                       @RequestHeader(name = AUTHORIZATION, required = false) String accessToken,
                                       @CookieValue(name = REFRESH_TOKEN_NAME, required = false) String refreshToken) {
        userService.logout(accessToken, refreshToken);
        deleteCookie(request, response, REFRESH_TOKEN_NAME);
        return success();
    }

    @Operation(summary = "이메일 인증코드 발송")
    @PostMapping({FIND_ID_URL, FIND_PW_URL})
    public CommonResponse<Void> sendEmailWithAuthCode(
            HttpServletRequest request,
            @AuthCodeSendInject final AuthCodeSendRequest authCodeSendRequest) {
        AuthCodeServiceBase authCodeService = getAuthCodeService(getAuthType(request));
        final var key = authCodeService.generateAndSaveRandomKey(authCodeSendRequest);
        emailService.sendEmail(authCodeSendRequest.getEmail(), "title", key);

        return success();
    }

    @Operation(summary = "인증 코드가 맞는지 검증")
    @PostMapping({FIND_ID_URL + "/check", FIND_PW_URL + "/check"})
    public CommonResponse<AuthCodeVerifyResponse> verifyAuthCode(
            HttpServletRequest request,
            @AuthCodeVerifyInject final AuthCodeVerifyRequest authCodeVerifyRequest) {
        AuthCodeServiceBase authCodeService = getAuthCodeService(getAuthType(request));
        AuthCodeVerifyResponse authCodeVerifyResponse = authCodeService.verifyAuthCode(authCodeVerifyRequest);

        return success(authCodeVerifyResponse);
    }

    @Operation(summary = "이미지 생성 및 정답 임시 저장")
    @PostMapping({FIND_ID_URL + "/captcha", FIND_PW_URL + "/captcha"})
    public void captchaGenerate(
            HttpServletRequest request,
            final CaptchaGenerateRequest captchaGenerateRequest,
            HttpServletResponse response) throws IOException {
        AuthType authType = getAuthType(request);
        Captcha captcha = captchaService.generateCaptchaAndSaveAnswer(captchaGenerateRequest.getEmail(), authType);

        BufferedImage image = captchaService.getImage(captcha);
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/png");
        ImageIO.write(image, "png", response.getOutputStream());
    }

    @Operation(summary = "캡챠 이미지 정답 검증")
    @PostMapping({FIND_PW_URL + "/captcha/check", FIND_PW_URL + "/captcha/check"})
    public CommonResponse<CaptchaValidateResponse> validateCaptcha(
            HttpServletRequest request,
            final CaptchaValidateRequest captchaValidateRequest) {
        AuthType authType = getAuthType(request);

        captchaService.validateCaptcha(captchaValidateRequest.getEmail(), captchaValidateRequest.getUserAnswer(), authType);

        return success();

    }

    @Operation(summary = "비밀번호 변경")
    @PostMapping("password/edit")
    public CommonResponse<Void> sendEmailWithAuthCode(
            HttpServletRequest request,
            @Valid final PWChangeRequest pwChangeRequest) {
        AuthCodeServiceBase authCodeService = getAuthCodeService(FIND_PASSWORD);
        authCodeService.changePassword(pwChangeRequest);

        return success();
    }

    @Operation(summary = "아이디 존재여부 확인")
    @PostMapping(FIND_ID_URL + "/exist")
    public CommonResponse<Boolean> isIdPresent(String username) {
        return success(userService.checkUsernameExist(username));
    }

    private AuthCodeServiceBase getAuthCodeService(AuthType authType) {
        return authCodeServices.stream()
                .filter(service -> service.support(authType))
                .findAny()
                .orElseThrow();
    }

    private AuthType getAuthType(HttpServletRequest request) {
        String uri = request.getRequestURI();
        AuthType authType = null;
        if (uri.contains(FIND_ID_URL)) {
            authType = FIND_ID;
        } else if (uri.contains(FIND_PW_URL)) {
            authType = FIND_PASSWORD;
        }

        return authType;
    }
}
