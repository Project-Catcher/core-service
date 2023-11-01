package com.catcher.resource;

import com.catcher.common.response.CommonResponse;
import com.catcher.core.domain.entity.enums.UserProvider;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.oauth.OAuthCreateRequest;
import com.catcher.core.dto.oauth.OAuthCreateResponse;
import com.catcher.core.dto.oauth.OAuthLoginRequest;
import com.catcher.infrastructure.oauth.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class OAuthController {
    private final OAuthService oAuthService;

    @GetMapping(value = {"/kakao", "/naver"})
    public CommonResponse<String> checkHistory(HttpServletRequest request, @RequestParam Map map) {
        return oAuthService.checkSignHistory(map, request.getRequestURI());
    }

    @PostMapping(value = {"/kakao", "/naver"})
    public CommonResponse<OAuthCreateResponse> signup(HttpServletRequest request, @RequestBody OAuthCreateRequest oAuthCreateRequest) {
        return oAuthService.signUp(oAuthCreateRequest, request.getRequestURI());
    }

    @PostMapping(value = {"/kakao/login", "/naver/login"})
    public CommonResponse<TokenDto> login(HttpServletRequest request, @RequestBody OAuthLoginRequest oAuthLoginRequest) {
        return oAuthService.login(oAuthLoginRequest, request.getRequestURI());
    }
}
