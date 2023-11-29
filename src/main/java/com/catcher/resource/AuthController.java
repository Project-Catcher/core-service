package com.catcher.resource;

import com.catcher.common.response.CommonResponse;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.catcher.config.JwtTokenProvider.setRefreshCookie;
import static com.catcher.utils.JwtUtils.REFRESH_TOKEN_NAME;

@Tag(name = "인증")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "토큰 재발행")
    @PostMapping("/reissue")
    public CommonResponse<String> reissue(@CookieValue(value = REFRESH_TOKEN_NAME, required = false) String refreshToken, HttpServletResponse response){
        TokenDto tokenDto = authService.reissueRefreshToken(refreshToken);
        setRefreshCookie(response, tokenDto.getRefreshToken());
        return CommonResponse.success(tokenDto.getAccessToken());
    }
}
