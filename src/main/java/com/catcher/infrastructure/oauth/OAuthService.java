package com.catcher.infrastructure.oauth;

import com.catcher.common.exception.BaseException;
import com.catcher.common.response.CommonResponse;
import com.catcher.config.JwtTokenProvider;
import com.catcher.core.domain.entity.User;
import com.catcher.core.domain.entity.enums.UserProvider;
import com.catcher.core.domain.entity.enums.UserRole;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.oauth.OAuthCreateRequest;
import com.catcher.core.dto.oauth.OAuthCreateResponse;
import com.catcher.core.dto.oauth.OAuthLoginRequest;
import com.catcher.core.dto.oauth.OAuthTokenResponse;
import com.catcher.datasource.UserRepository;
import com.catcher.infrastructure.oauth.properties.OAuthProperties;
import com.catcher.infrastructure.oauth.user.OAuthUserInfo;
import com.catcher.infrastructure.oauth.user.OAuthUserInfoFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static com.catcher.common.BaseResponseStatus.INVALID_USER_PW;
import static com.catcher.common.response.CommonResponse.*;
import static jakarta.servlet.http.HttpServletResponse.SC_CONFLICT;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {
    private List<OAuthProperties> oAuthPropertiesList;
    private final ApplicationContext applicationContext;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @PostConstruct
    void postConstruct() {
        this.oAuthPropertiesList = new ArrayList<>();
        String[] names = applicationContext.getBeanNamesForType(OAuthProperties.class);
        for (String name : names) {
            OAuthProperties bean = (OAuthProperties) applicationContext.getBean(name);
            this.oAuthPropertiesList.add(bean);
        }
    }

    @Transactional(readOnly = true)
    public CommonResponse<String> checkSignHistory(Map map, String path) {
        OAuthProperties oAuthProperties = getOAuthPropertiesByPath(path);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(oAuthProperties.getJsonBody(map), httpHeaders);
        ResponseEntity<OAuthTokenResponse> response = restTemplate.postForEntity(oAuthProperties.getTokenUri(), request, OAuthTokenResponse.class);

        String accessToken = response.getBody().getAccess_token();
        String email = getEmailByAccessToken(accessToken, oAuthProperties);

        User user = userRepository.findByEmail(email).orElse(null);

        if(user == null) {
            return success(SC_OK, accessToken);
        }

        return failure(SC_CONFLICT, "이미 가입한 이메일이 있습니다.");
    }

    @Transactional(readOnly = true)
    public CommonResponse<TokenDto> login(OAuthLoginRequest oAuthLoginRequest, String path) {
        OAuthProperties oAuthProperties = getOAuthPropertiesByPath(path);
        String accessToken = oAuthLoginRequest.getAccessToken();
        String email = getEmailByAccessToken(accessToken, oAuthProperties);

        User user = userRepository.findByEmail(email).orElseThrow();// TODO : Custom Exception Handling

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            user.getPassword()
                    )
            );

            TokenDto tokenDto = new TokenDto(
                    jwtTokenProvider.createAccessToken(authentication),
                    jwtTokenProvider.createRefreshToken(authentication)
            );

            return CommonResponse.success(SC_OK, tokenDto);

        }catch(BadCredentialsException e){
            log.error(INVALID_USER_PW.getMessage());
            throw new BaseException(INVALID_USER_PW);
        }

    }

    @Transactional
    public CommonResponse<OAuthCreateResponse> signUp(OAuthCreateRequest oAuthCreateRequest, String path) {
        OAuthProperties oAuthProperties = getOAuthPropertiesByPath(path);
        String accessToken = oAuthCreateRequest.getAccessToken();
        String email = getEmailByAccessToken(accessToken, oAuthProperties);

        userRepository.findByEmail(email).orElseThrow(); // TODO : Custom Exception  Handling

        User user = createOAuthUser(oAuthCreateRequest, oAuthProperties, email);

        return CommonResponse.success(SC_OK, OAuthCreateResponse.from(user));
    }

    private String getEmailByAccessToken(String accessToken, OAuthProperties oAuthProperties) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(oAuthProperties.getUserInfoUri(), request, Map.class);
            OAuthUserInfo oAuthUserInfo = OAuthUserInfoFactory.getOAuthUserInfo(oAuthProperties.getProvider(), response.getBody());

            return oAuthUserInfo.getEmail();
        } catch (RestClientException ex) {
            throw new RuntimeException();
        }
    }

    private User createOAuthUser(OAuthCreateRequest oAuthCreateRequest, OAuthProperties oAuthProperties, String email) {
        User user = User.builder()
                .username(UUID.randomUUID().toString())
                .password(passwordEncoder.encode("NO_PASS"))
                .name(UUID.randomUUID().toString())
                .email(email)
                .role(oAuthCreateRequest.getRole() == 0 ? UserRole.ADMIN : UserRole.USER)
                .userProvider(oAuthProperties.getProvider())
                .phone(oAuthCreateRequest.getPhone())
                .userLocationTerm(oAuthCreateRequest.getLocationTerm())
                .userPrivacyTerm(oAuthCreateRequest.getPrivacyTerm())
                .userServiceTerm(oAuthCreateRequest.getServiceTerm())
                .userAgeTerm(oAuthCreateRequest.getAgeTerm())
                .nickname(oAuthCreateRequest.getNickname())
                .introduceContent(oAuthCreateRequest.getIntroduceContent())
                .userMarketingTerm(oAuthCreateRequest.getMarketingTerm())
                .build();
        return userRepository.save(user);
    }

    private OAuthProperties getOAuthPropertiesByPath(String path) {
        UserProvider userProvider = path.contains("/kakao") ? UserProvider.KAKAO : UserProvider.NAVER;
        OAuthProperties oAuthProperties = getOAuthPropertiesList(userProvider);
        return oAuthProperties;
    }

    private OAuthProperties getOAuthPropertiesList(UserProvider userProvider) {
        return oAuthPropertiesList.stream()
                .filter(propertyBase -> propertyBase.support(userProvider))
                .findFirst()
                .orElseThrow();
    }
}
