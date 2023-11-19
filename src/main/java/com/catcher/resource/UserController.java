package com.catcher.resource;

import com.catcher.common.response.CommonResponse;
import com.catcher.core.dto.user.UserCreateRequest;
import com.catcher.core.service.UserService;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.user.UserLoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.catcher.common.response.CommonResponse.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;

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
}
