package com.catcher.resource;

import cn.apiclub.captcha.Captcha;
import com.catcher.common.response.CommonResponse;
import com.catcher.core.domain.entity.User;
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
import com.catcher.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static com.catcher.common.response.CommonResponse.success;

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
    public CommonResponse<TokenDto> signUp(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        return success(userService.signUpUser(userCreateRequest));
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public CommonResponse<TokenDto> login(@Valid @RequestBody UserLoginRequest userLoginReqDto) {
        return success(userService.login(userLoginReqDto));
    }

    //TODO: 삭제예정
    @PostMapping("/test")
    public void test(@CurrentUser User user) {
        log.info("user = {}", user);
    }


    // TODO: 제목 교체
    @Operation(summary = "이메일 인증코드 발송")
    @PostMapping("/create-authcode/email")
    public CommonResponse<Void> sendEmailWithAuthCode(final AuthCodeSendRequest authCodeSendRequest) {

        final var key = authCodeService.generateAndSaveRandomKey(authCodeSendRequest.getEmail());
        emailService.sendEmail(authCodeSendRequest.getEmail(), "title", key);
        return success();
    }

    // TODO: 응답 타입은 따로 생각해보기
    @Operation(summary = "인증 코드가 맞는지 검증")
    @GetMapping("/check-authcode/email")
    public CommonResponse<AuthCodeVerifyResponse> verifyAuthCode(final AuthCodeVerifyRequest authCodeVerifyRequest) {
        final boolean isVerified = authCodeService.verifyAuthCode(authCodeVerifyRequest.getEmail(), authCodeVerifyRequest.getAuthCode());

        return success(new AuthCodeVerifyResponse(isVerified));
    }

    @Operation(summary = "캡챠 이미지 생성 및 정답 임시 저장")
    @PostMapping("/captcha/email")
    public void captchaGenerate(final CaptchaGenerateRequest captchaGenerateRequest, HttpServletResponse response) throws IOException {
        Captcha captcha = captchaService.generateCaptchaAndSaveAnswer(captchaGenerateRequest.getEmail());

        BufferedImage image = captchaService.getImage(captcha);
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");
        ImageIO.write(image, "jpeg", response.getOutputStream());
    }

    @Operation(summary = "캡챠 이미지 정답 검증")
    @PostMapping("/captcha/validate/email")
    public CommonResponse<CaptchaValidateResponse> validateCaptcha(final CaptchaValidateRequest captchaValidateRequest) {
        final boolean isValidated = captchaService.validateCaptcha(captchaValidateRequest.getEmail(), captchaValidateRequest.getUserAnswer());

        return success(new CaptchaValidateResponse(isValidated));

    }
}
