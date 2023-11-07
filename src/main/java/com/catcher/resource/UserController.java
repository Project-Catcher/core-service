package com.catcher.resource;

import com.catcher.common.exception.BaseException;
import com.catcher.common.response.BaseResponse;
import com.catcher.core.dto.user.UserCreateRequest;
import com.catcher.core.dto.user.UserCreateResponse;
import com.catcher.core.dto.user.UserResponse;
import com.catcher.core.service.UserService;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.user.UserLoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;

    @Operation(summary = "회원 가입")
    @PostMapping("/signup")
    public BaseResponse<TokenDto> signUp(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        return new BaseResponse<>(userService.signUpUser(userCreateRequest));
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public BaseResponse<TokenDto> login(@Valid @RequestBody UserLoginRequest userLoginReqDto) {
        return new BaseResponse<>(userService.login(userLoginReqDto));
    }
}
