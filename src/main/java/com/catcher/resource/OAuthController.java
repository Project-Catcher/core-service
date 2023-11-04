package com.catcher.resource;

import com.catcher.common.response.BaseResponse;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.oauth.OAuthCreateRequest;
import com.catcher.core.dto.oauth.OAuthHistoryResponse;
import com.catcher.core.dto.user.UserCreateResponse;
import com.catcher.core.service.OAuthService;
import com.catcher.infrastructure.KmsService;
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
    private final KmsService kmsService;
    private final OAuthService oAuthService;
    @Value("${oauth2.client.registration.naver.client-id}")
    private String naverClientId;
    @Value("${oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @GetMapping(value = {"/test/naver", "/test/kakao"})
    public void test(HttpServletRequest request, HttpServletResponse response) throws IOException { // 이 부분은 프론트에서 진행 추후 삭제 예정
        String requestURI = request.getRequestURI();
        if (requestURI.contains("kakao")) { // kakao
            response.sendRedirect("https://kauth.kakao.com/oauth/authorize?client_id=" + kmsService.decrypt(kakaoClientId) + "&redirect_uri=http://localhost:8080/oauth/kakao&response_type=code");
        } else { // naver
            response.sendRedirect("https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=" + kmsService.decrypt(naverClientId) + "&state=h2m4lh6brdvd3gbgd3vnsd8esg&redirect_uri=http://localhost:8080/oauth/naver");
        }
    }

    @GetMapping(value = {"/test/naver/login", "/test/kakao/login"})
    public void testLogin(HttpServletRequest request, HttpServletResponse response) throws IOException { // 이 부분은 프론트에서 진행 추후 삭제 예정
        String requestURI = request.getRequestURI();
        if (requestURI.contains("kakao")) { // kakao
            response.sendRedirect("https://kauth.kakao.com/oauth/authorize?client_id=" + kmsService.decrypt(kakaoClientId) + "&redirect_uri=http://localhost:8080/oauth/kakao/login&response_type=code");
        } else { // naver
            response.sendRedirect("https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=" + kmsService.decrypt(naverClientId) + "&state=h2m4lh6brdvd3gbgd3vnsd8esg&redirect_uri=http://localhost:8080/oauth/naver/login");
        }
    }

    @GetMapping(value = {"/kakao", "/naver"})
    public OAuthHistoryResponse checkHistory(HttpServletRequest request, @RequestParam Map params) {
        return oAuthService.checkSignHistory(params, request.getRequestURI());
    }

    @GetMapping(value = {"/kakao/login", "/naver/login"})
    public BaseResponse<TokenDto> login(HttpServletRequest request, @RequestParam Map map) {
        return new BaseResponse<>(oAuthService.login(map, request.getRequestURI()));
    }

    @PostMapping(value = {"/kakao", "/naver"})
    public BaseResponse<UserCreateResponse> signUp(HttpServletRequest request, @RequestBody OAuthCreateRequest oAuthCreateRequest) {
        return new BaseResponse<>(oAuthService.signUp(oAuthCreateRequest, request.getRequestURI()));
    }


}
