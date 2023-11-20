package com.catcher.resource;

import com.catcher.common.response.CommonResponse;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.oauth.OAuthCreateRequest;
import com.catcher.core.dto.oauth.OAuthHistoryResponse;
import com.catcher.core.service.OAuthService;
import com.catcher.infrastructure.KmsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class OAuthController {
    private final KmsService kmsService;
    private final OAuthService oAuthService;
    @Value("${oauth2.client.registration.naver.client-id}")
    private String naverClientId;
    @Value("${oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    //TODO: 삭제 예정, 프론트에서 진행할 부분 - hg
    @GetMapping(value = {"/test/naver/signup", "/test/kakao/signup"})
    public void testSignUp(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestURI = request.getRequestURI();
        if (requestURI.contains("kakao")) { // kakao
            response.sendRedirect("https://kauth.kakao.com/oauth/authorize?client_id=" + kmsService.decrypt(kakaoClientId) + "&redirect_uri=http://localhost:8080/oauth/kakao&response_type=code");
        } else { // naver
            response.sendRedirect("https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=" + kmsService.decrypt(naverClientId) + "&state=h2m4lh6brdvd3gbgd3vnsd8esg&redirect_uri=http://localhost:8080/oauth/naver");
        }
    }

    //TODO: 삭제 예정, 프론트에서 진행할 부분 - hg
    @GetMapping(value = {"/test/naver/login", "/test/kakao/login"})
    public void testLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestURI = request.getRequestURI();
        if (requestURI.contains("kakao")) { // kakao
            response.sendRedirect("https://kauth.kakao.com/oauth/authorize?client_id=" + kmsService.decrypt(kakaoClientId) + "&redirect_uri=http://localhost:8080/oauth/kakao/login&response_type=code");
        } else { // naver
            response.sendRedirect("https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=" + kmsService.decrypt(naverClientId) + "&state=h2m4lh6brdvd3gbgd3vnsd8esg&redirect_uri=http://localhost:8080/oauth/naver/login");
        }
    }

    @GetMapping(value = {"/kakao", "/naver"})
    public CommonResponse<OAuthHistoryResponse> checkHistory(HttpServletRequest request, @RequestParam Map params) {
        return CommonResponse.success(oAuthService.checkSignHistory(params, request.getRequestURI()));
    }

    @GetMapping(value = {"/kakao/login", "/naver/login"})
    public CommonResponse<TokenDto> login(HttpServletRequest request, @RequestParam Map map) {
        return CommonResponse.success(oAuthService.login(map, request.getRequestURI()));
    }

    @PostMapping(value = {"/kakao", "/naver"})
    public CommonResponse<TokenDto> signUp(HttpServletRequest request, @Valid @RequestBody OAuthCreateRequest oAuthCreateRequest) {
        return CommonResponse.success(oAuthService.signUp(oAuthCreateRequest, request.getRequestURI()));
    }
}
