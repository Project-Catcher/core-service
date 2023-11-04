package com.catcher.core.service;

import com.catcher.common.exception.BaseException;
import com.catcher.config.JwtTokenProvider;
import com.catcher.core.database.DBManager;
import com.catcher.core.database.UserRepository;
import com.catcher.core.domain.entity.User;
import com.catcher.core.domain.entity.enums.UserProvider;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.oauth.OAuthCreateRequest;
import com.catcher.core.dto.oauth.OAuthHistoryResponse;
import com.catcher.core.dto.user.UserCreateResponse;
import com.catcher.infrastructure.oauth.OAuthHandler;
import com.catcher.infrastructure.oauth.OAuthTokenResponse;
import com.catcher.infrastructure.oauth.properties.OAuthProperties;
import com.catcher.infrastructure.oauth.properties.OAuthPropertiesHandler;
import com.catcher.infrastructure.oauth.user.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Map;

import static com.catcher.common.BaseResponseStatus.*;
import static com.catcher.core.domain.entity.enums.UserRole.USER;
import static com.catcher.utils.JwtUtils.REFRESH_TOKEN_EXPIRATION_MILLIS;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuthService {
    private final OAuthPropertiesHandler oAuthPropertiesHandler;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final OAuthHandler oAuthHandler;
    private final DBManager dbManager;

    @Transactional(readOnly = true)
    public OAuthHistoryResponse checkSignHistory(Map map, String path) {
        OAuthProperties oAuthProperties = getOAuthProperties(path);

        OAuthTokenResponse oAuthTokenResponse = oAuthHandler.getSignUpToken(oAuthProperties, map);
        OAuthUserInfo oAuthUserInfo = oAuthHandler.getOAuthUserInfo(oAuthProperties, oAuthTokenResponse.getAccessToken());

        String id = oAuthUserInfo.getId();


        validateExistsUsername(id);
        validateExistsEmail(oAuthUserInfo.getEmail());

        return new OAuthHistoryResponse(oAuthTokenResponse.getAccessToken(), oAuthUserInfo.getEmail());
    }

    @Transactional(readOnly = true)
    public TokenDto login(Map map, String path) {
        OAuthProperties oAuthProperties = getOAuthProperties(path);

        OAuthTokenResponse loginToken = oAuthHandler.getLoginToken(oAuthProperties, map);
        OAuthUserInfo oAuthUserInfo = oAuthHandler.getOAuthUserInfo(oAuthProperties, loginToken.getAccessToken());

        User user = userRepository.findByUsername(oAuthUserInfo.getId()).orElseThrow(
                () -> new BaseException(USERS_NOT_EXISTS)
        );

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        "NO-PASS"
                )
        );

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        dbManager.putValue(authentication.getName(), refreshToken, REFRESH_TOKEN_EXPIRATION_MILLIS);

        return new TokenDto(accessToken, refreshToken);
    }

    @Transactional
    public UserCreateResponse signUp(OAuthCreateRequest oAuthCreateRequest, String path) {
        OAuthProperties oAuthProperties = getOAuthProperties(path);
        String accessToken = oAuthCreateRequest.getAccessToken();
        OAuthUserInfo oAuthUserInfo = oAuthHandler.getOAuthUserInfo(oAuthProperties, accessToken);

        validateExistsUsername(oAuthUserInfo.getId());
        validateExistsEmail(oAuthUserInfo.getEmail());

        User user = createUser(oAuthCreateRequest, oAuthUserInfo);

        userRepository.save(user);

        return UserCreateResponse.from(user);
    }

    private User createUser(OAuthCreateRequest oAuthCreateRequest, OAuthUserInfo oAuthUserInfo) {
        return User.builder()
                .username(oAuthUserInfo.getId())
                .password(passwordEncoder.encode("NO-PASS"))
                .phone(oAuthCreateRequest.getPhone())
                .email(oAuthUserInfo.getEmail())
                .nickname(oAuthUserInfo.getProvider().name() + "_" + oAuthUserInfo.getId())
                .userProvider(oAuthUserInfo.getProvider())
                .role(USER)
                .userAgeTerm(oAuthCreateRequest.getAgeTerm())
                .userServiceTerm(oAuthCreateRequest.getServiceTerm())
                .userPrivacyTerm(oAuthCreateRequest.getPrivacyTerm())
                .userLocationTerm(oAuthCreateRequest.getLocationTerm())
                .userMarketingTerm(oAuthCreateRequest.getMarketingTerm())
                .build();
    }

    private void validateExistsUsername(String username) {
        if(userRepository.findByUsername(username).isPresent()) {
            throw new BaseException(USERS_DUPLICATED_USER);
        }
    }

    private void validateExistsEmail(String email) {
        if(userRepository.findByEmail(email).isPresent()) {
            throw  new BaseException(USERS_DUPLICATED_USER_EMAIL);
        }
    }

    private OAuthProperties getOAuthProperties(String path) {
        return Arrays.stream(UserProvider.values())
                .filter(provider -> path.contains(provider.name().toLowerCase()))
                .findAny()
                .map(provider -> oAuthPropertiesHandler.getOAuthProperties(provider))
                .orElseThrow(() -> new BaseException(INVALID_USER_OAUTH_TYPE));
    }
}
