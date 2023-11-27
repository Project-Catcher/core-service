package com.catcher.resource;

import com.catcher.common.response.CommonResponse;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.user.UserCreateRequest;
import com.catcher.core.dto.user.UserLoginRequest;
import com.catcher.core.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.catcher.common.response.CommonResponse.success;
import static com.catcher.config.JwtTokenProvider.setRefreshCookie;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;

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
}
