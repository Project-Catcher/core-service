package com.catcher.resource;

import com.catcher.common.response.CommonResponse;
import com.catcher.core.domain.entity.User;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.user.UserCreateRequest;
import com.catcher.core.dto.user.UserLoginRequest;
import com.catcher.core.service.AuthCodeService;
import com.catcher.core.service.EmailService;
import com.catcher.core.service.UserService;
import com.catcher.resource.request.AuthCodeSendRequest;
import com.catcher.resource.request.AuthCodeVerifyRequest;
import com.catcher.resource.response.AuthCodeVerifyResponse;
import com.catcher.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.catcher.common.response.CommonResponse.success;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;
    private final EmailService emailService;
    private final AuthCodeService authCodeService;

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
    @GetMapping("/check-authcode/email/{email}")
    public CommonResponse<AuthCodeVerifyResponse> verifyAuthCode(final AuthCodeVerifyRequest authCodeVerifyRequest) {
        final boolean isVerified = authCodeService.verifyAuthCode(authCodeVerifyRequest.getEmail(), authCodeVerifyRequest.getAuthCode());

        return success(new AuthCodeVerifyResponse(isVerified));
    }
}
