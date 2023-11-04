package com.catcher.resource;

import com.catcher.common.response.BaseResponse;
import com.catcher.core.service.AuthService;
import com.catcher.core.dto.TokenDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "토큰 재발행")
    @PostMapping("/reissue")
    public BaseResponse reissue(String refreshToken){
        return new BaseResponse<>(authService.reissueRefreshToken(refreshToken));
    }
}
