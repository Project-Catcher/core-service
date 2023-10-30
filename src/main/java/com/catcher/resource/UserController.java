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
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
@Log4j2
public class UserController {
    private final UserService userService;

    @Operation(summary = "회원 가입")
    @PostMapping("/signup")
    public BaseResponse<UserCreateResponse> signUp(@Valid @RequestBody UserCreateRequest userCreateRequest, BindingResult br) {
        // 형식적 validation
        if (br.hasErrors()){
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            log.error(errorName);
            return new BaseResponse<>(errorName);
        }

        try {
            return new BaseResponse<>(userService.signUpUser(userCreateRequest));
        } catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public BaseResponse<TokenDto> login(@Valid @RequestBody UserLoginRequest userLoginReqDto, BindingResult br) throws BaseException {

        if (br.hasErrors()){
            String errorName = br.getAllErrors().get(0).getDefaultMessage();
            log.error(errorName);
            return new BaseResponse<>(errorName);
        }

        try {
            return new BaseResponse<>(userService.login(userLoginReqDto));
        } catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    @Operation(summary = "사용자 조회")
    @GetMapping("/{id}")
    public BaseResponse<UserResponse> getUser(@PathVariable("id") Long uid) {
        try {
            return new BaseResponse<>(userService.getUser(uid));
        } catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }
}