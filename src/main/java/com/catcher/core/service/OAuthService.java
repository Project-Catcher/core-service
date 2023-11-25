package com.catcher.core.service;

import com.catcher.common.BaseResponseStatus;
import com.catcher.common.exception.BaseException;
import com.catcher.common.exception.OAuthException;
import com.catcher.config.JwtTokenProvider;
import com.catcher.core.database.DBManager;
import com.catcher.core.database.UserRepository;
import com.catcher.core.domain.entity.User;
import com.catcher.core.domain.entity.enums.UserProvider;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.oauth.OAuthCreateRequest;
import com.catcher.core.dto.oauth.OAuthHistoryResponse;
import com.catcher.infrastructure.oauth.OAuthTokenResponse;
import com.catcher.infrastructure.oauth.handler.OAuthHandler;
import com.catcher.infrastructure.oauth.handler.OAuthHandlerFactory;
import com.catcher.infrastructure.oauth.properties.OAuthProperties;
import com.catcher.infrastructure.oauth.user.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static com.catcher.common.BaseResponseStatus.*;
import static com.catcher.utils.JwtUtils.REFRESH_TOKEN_EXPIRATION_MILLIS;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final OAuthHandlerFactory oAuthHandlerFactory;
    private final DBManager dbManager;

    @Transactional(readOnly = true)
    public OAuthHistoryResponse checkSignHistory(Map map, String path) {
        OAuthHandler oAuthHandler = getOAuthHandler(path);
        try {
            OAuthTokenResponse oAuthTokenResponse = oAuthHandler.handleToken(signUpJsonProperty(oAuthHandler, map));
            OAuthUserInfo oAuthUserInfo = oAuthHandler.handleUserInfo(oAuthTokenResponse.getAccessToken());

            String id = oAuthUserInfo.getId();

            checkDuplicateUser(userRepository.findByUsername(id), USERS_DUPLICATED_USER);
            checkDuplicateUser(userRepository.findByEmail(id), USERS_DUPLICATED_USER_EMAIL);

            return new OAuthHistoryResponse(oAuthTokenResponse.getAccessToken(), oAuthUserInfo.getEmail());
        } catch (OAuthException e) {
            oAuthHandler.invalidateToken(e.getAccessToken());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public TokenDto login(Map map, String path) {
        OAuthHandler oAuthHandler = getOAuthHandler(path);
        try {
            OAuthTokenResponse oAuthTokenResponse = oAuthHandler.handleToken(getLoginProperty(oAuthHandler, map));
            OAuthUserInfo oAuthUserInfo = oAuthHandler.handleUserInfo(oAuthTokenResponse.getAccessToken());

            oAuthHandler.invalidateToken(oAuthTokenResponse.getAccessToken());

            return checkAuthenticationAndGetTokenDto(oAuthUserInfo.getId());
        } catch (OAuthException e) {
            oAuthHandler.invalidateToken(e.getAccessToken());
            throw e;
        }
    }

    @Transactional
    public TokenDto signUp(OAuthCreateRequest oAuthCreateRequest, String path) {
        OAuthHandler oAuthHandler = getOAuthHandler(path);
        String accessToken = oAuthCreateRequest.getAccessToken();

        try {
            OAuthUserInfo oAuthUserInfo = oAuthHandler.handleUserInfo(accessToken);

            checkDuplicateUser(userRepository.findByUsername(oAuthUserInfo.getId()), USERS_DUPLICATED_USER);
            checkDuplicateUser(userRepository.findByEmail(oAuthUserInfo.getEmail()), USERS_DUPLICATED_USER_EMAIL);
            checkDuplicateUser(userRepository.findByNickname(oAuthCreateRequest.getNickname()), USERS_DUPLICATED_NICKNAME);
            checkDuplicateUser(userRepository.findByPhone(oAuthCreateRequest.getPhone()), USERS_DUPLICATED_PHONE);

            User user = createUser(oAuthCreateRequest, oAuthUserInfo);
            userRepository.save(user);

            oAuthHandler.invalidateToken(accessToken);

            return checkAuthenticationAndGetTokenDto(user.getUsername());
        } catch (OAuthException e) {
            oAuthHandler.invalidateToken(accessToken);
            throw e;
        }
    }

    private User createUser(OAuthCreateRequest oAuthCreateRequest, OAuthUserInfo oAuthUserInfo) {
        return User.builder()
                .username(oAuthUserInfo.getId())
                .password(passwordEncoder.encode("NO-PASS"))
                .phone(oAuthCreateRequest.getPhone())
                .email(oAuthUserInfo.getEmail())
                .nickname(oAuthCreateRequest.getNickname())
                .userProvider(oAuthUserInfo.getProvider())
                .phoneAuthentication(null)
                .userAgeTerm(oAuthCreateRequest.getAgeTerm())
                .userServiceTerm(oAuthCreateRequest.getServiceTerm())
                .userPrivacyTerm(oAuthCreateRequest.getPrivacyTerm())
                .emailMarketingTerm(oAuthCreateRequest.getMarketingTerm())
                .phoneMarketingTerm(oAuthCreateRequest.getMarketingTerm())
                .build();
    }

    private TokenDto checkAuthenticationAndGetTokenDto(String username) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, "NO-PASS")
            );

            String accessToken = jwtTokenProvider.createAccessToken(authentication);
            String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

            dbManager.putValue(username, refreshToken, REFRESH_TOKEN_EXPIRATION_MILLIS);

            return new TokenDto(accessToken, refreshToken);
        } catch (BadCredentialsException e) {
            log.error(INVALID_USER_PW.getMessage());
            throw new BaseException(INVALID_USER_PW);
        } catch (InternalAuthenticationServiceException e) {
            throw new BaseException(INVALID_USER_NAME);
        }
    }

    private OAuthHandler getOAuthHandler(String path) {
        UserProvider userProvider = resolveUserProvider(path);
        return oAuthHandlerFactory.getOAuthHandler(userProvider);
    }

    private UserProvider resolveUserProvider(String path) {
        return Arrays.stream(UserProvider.values())
                .filter(provider -> path.contains(provider.name().toLowerCase()))
                .findAny()
                .orElseThrow(() -> new BaseException(INVALID_USER_OAUTH_TYPE));
    }

    private void checkDuplicateUser(Optional<User> userOptional, BaseResponseStatus responseStatus) {
        if(userOptional.isPresent()) {
            throw new BaseException(responseStatus);
        }
    }

    private Supplier<Map> signUpJsonProperty(OAuthHandler oAuthHandler, Map map) {
        return () -> {
            OAuthProperties oAuthProperties = oAuthHandler.getOAuthProperties();
            return oAuthProperties.getSignUpJsonBody(map);
        };
    }

    private Supplier<Map> getLoginProperty(OAuthHandler oAuthHandler, Map map) {
        return () -> {
            OAuthProperties oAuthProperties = oAuthHandler.getOAuthProperties();
            return oAuthProperties.getLoginJsonBody(map);
        };
    }
}
