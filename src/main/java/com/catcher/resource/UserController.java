package com.catcher.resource;

import cn.apiclub.captcha.Captcha;
import com.catcher.common.response.CommonResponse;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.user.UserCreateRequest;
import com.catcher.core.dto.user.UserLoginRequest;
import com.catcher.core.service.AuthCodeService;
import com.catcher.core.service.CaptchaService;
import com.catcher.core.service.EmailService;
import com.catcher.core.service.UserService;
import com.catcher.resource.request.AuthCodeSendRequest;
import com.catcher.resource.request.AuthCodeVerifyRequest;
import com.catcher.resource.request.CaptchaGenerateRequest;
import com.catcher.resource.request.CaptchaValidateRequest;
import com.catcher.resource.response.AuthCodeVerifyResponse;
import com.catcher.resource.response.CaptchaValidateResponse;
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

import static com.catcher.common.response.CommonResponse.success;
import static com.catcher.config.JwtTokenProvider.setRefreshCookie;
import static com.catcher.utils.HttpServletUtils.deleteCookie;
import static com.catcher.utils.JwtUtils.REFRESH_TOKEN_NAME;
import static com.catcher.utils.KeyGenerator.AuthType.FIND_ID;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;
    private final EmailService emailService;
    private final AuthCodeService authCodeService;
    private final CaptchaService captchaService;

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

    @Operation(summary = "ID 찾기 이메일 인증코드 발송")
    @PostMapping("/create-authcode/email")
    public CommonResponse<Void> sendEmailWithAuthCode(final AuthCodeSendRequest authCodeSendRequest) {
        final var key = authCodeService.generateAndSaveRandomKey(authCodeSendRequest.getEmail(), FIND_ID);
        emailService.sendEmail(authCodeSendRequest.getEmail(), "title", key);

        return success();
    }

    // TODO: 응답 타입은 따로 생각해보기
    @Operation(summary = "ID 찾기 인증 코드가 맞는지 검증")
    @PostMapping("/check-authcode/email")
    public CommonResponse<AuthCodeVerifyResponse> verifyAuthCode(final AuthCodeVerifyRequest authCodeVerifyRequest) {
        final boolean isVerified = authCodeService.verifyAuthCode(authCodeVerifyRequest.getEmail(), authCodeVerifyRequest.getAuthCode(), FIND_ID);

        return success(new AuthCodeVerifyResponse(isVerified));
    }

    @Operation(summary = "ID 찾기 캡챠 이미지 생성 및 정답 임시 저장")
    @PostMapping("/captcha/email")
    public void captchaGenerate(final CaptchaGenerateRequest captchaGenerateRequest, HttpServletResponse response) throws IOException {
        Captcha captcha = captchaService.generateCaptchaAndSaveAnswer(captchaGenerateRequest.getEmail(), FIND_ID);

        BufferedImage image = captchaService.getImage(captcha);
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/png");
        ImageIO.write(image, "png", response.getOutputStream());
    }

    @Operation(summary = "ID 찾기 캡챠 이미지 정답 검증")
    @PostMapping("/captcha/validate/email")
    public CommonResponse<CaptchaValidateResponse> validateCaptcha(final CaptchaValidateRequest captchaValidateRequest) {
        final boolean isValidated = captchaService.validateCaptcha(captchaValidateRequest.getEmail(), captchaValidateRequest.getUserAnswer(), FIND_ID);

        return success(new CaptchaValidateResponse(isValidated));

    }
}
