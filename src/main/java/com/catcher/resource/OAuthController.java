package com.catcher.resource;

import com.catcher.common.response.CommonResponse;
import com.catcher.core.domain.entity.enums.UserProvider;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.oauth.OAuthCreateRequest;
import com.catcher.core.dto.oauth.OAuthCreateResponse;
import com.catcher.core.dto.oauth.OAuthLoginRequest;
import com.catcher.infrastructure.KmsService;
import com.catcher.infrastructure.oauth.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class OAuthController {
    private final OAuthService oAuthService;
    private final KmsService kmsService;

    @Value("${oauth2.client.registration.naver.client-id}")
    private String naverClientId;
    @Value("${oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @GetMapping(value = {"/test/naver", "/test/kakao"}) // TODO : 테스트용 & 이 부분은 프론트가 진행 -> 추후 삭제 예정 - hg
    public void test(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestURI = request.getRequestURI();
        if(requestURI.contains("kakao")) { // kakao
            response.sendRedirect("https://kauth.kakao.com/oauth/authorize?client_id=" + kmsService.decrypt(kakaoClientId) + "&redirect_uri=http://localhost:8080/oauth/kakao&response_type=code");
        } else { // naver
            response.sendRedirect("https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=" + kmsService.decrypt(naverClientId) + "&state=h2m4lh6brdvd3gbgd3vnsd8esg&redirect_uri=http://localhost:8080/oauth/naver");
        }
    }

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
