package com.catcher.core.service;

import com.catcher.common.BaseResponseStatus;
import com.catcher.common.exception.BaseException;
import com.catcher.config.JwtTokenProvider;
import com.catcher.core.database.DBManager;
import com.catcher.core.database.UserRepository;
import com.catcher.core.domain.entity.User;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.user.UserCreateRequest;
import com.catcher.core.dto.user.UserLoginRequest;
import com.catcher.security.CatcherUser;
import com.catcher.utils.KeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;

import static com.catcher.common.BaseResponseStatus.*;
import static com.catcher.core.domain.entity.BaseTimeEntity.zoneId;
import static com.catcher.core.domain.entity.enums.UserProvider.CATCHER;
import static com.catcher.core.domain.entity.enums.UserRole.USER;
import static com.catcher.utils.JwtUtils.REFRESH_TOKEN_EXPIRATION_MILLIS;
import static com.catcher.utils.KeyGenerator.AuthType.*;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final DBManager dbManager;
    private final AuthService authService;

    @Transactional
    public TokenDto signUpUser(UserCreateRequest userCreateRequest) {
        checkDuplicateUser(userRepository.findByUsername(userCreateRequest.getUsername()), USERS_DUPLICATED_USER_NAME);
        checkDuplicateUser(userRepository.findByNickname(userCreateRequest.getNickname()), USERS_DUPLICATED_NICKNAME);
        checkDuplicateUser(userRepository.findByPhone(userCreateRequest.getPhone()), USERS_DUPLICATED_PHONE);
        checkDuplicateUser(userRepository.findByEmail(userCreateRequest.getEmail()), USERS_DUPLICATED_USER_EMAIL);

        User user = createUser(userCreateRequest);

        userRepository.save(user);

        return checkAuthenticationAndGetTokenDto(userCreateRequest.getUsername(), userCreateRequest.getPassword());
    }

    public TokenDto login(UserLoginRequest userLoginReqDto) {
        return checkAuthenticationAndGetTokenDto(userLoginReqDto.getUsername(), userLoginReqDto.getPassword());
    }

    public void logout(String accessToken, String refreshToken) {
        authService.discardAccessToken(accessToken);
        authService.discardRefreshToken(refreshToken);
    }

    public boolean isExistsUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    private TokenDto checkAuthenticationAndGetTokenDto(String username, String password) {
        try {
            CatcherUser authentication = (CatcherUser) authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            User user = (User) authentication.getCredentials();
            checkOAuthUser(user);

            String accessToken = jwtTokenProvider.createAccessToken(authentication);
            String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

            dbManager.putValue(KeyGenerator.generateKey(username, REFRESH_TOKEN), refreshToken, REFRESH_TOKEN_EXPIRATION_MILLIS);

            return new TokenDto(accessToken, refreshToken);
        } catch (BadCredentialsException e) {
            log.error(INVALID_USER_PW.getMessage());
            throw new BaseException(INVALID_USER_PW);
        } catch (InternalAuthenticationServiceException e) {
            throw new BaseException(INVALID_USER_NAME);
        }
    }

    private void checkDuplicateUser(Optional<User> optionalUser, BaseResponseStatus responseStatus) {
        if (optionalUser.isPresent()) {
            throw new BaseException(responseStatus);
        }
    }

    private User createUser(UserCreateRequest userCreateRequest) {
        return User.builder()
                .username(userCreateRequest.getUsername())
                .password(passwordEncoder.encode(userCreateRequest.getPassword()))
                .phone(userCreateRequest.getPhone())
                .email(userCreateRequest.getEmail())
                .nickname(userCreateRequest.getNickname())
                .userProvider(CATCHER)
                .userRole(USER)
                .phoneAuthentication(ZonedDateTime.now(zoneId))
                .userAgeTerm(userCreateRequest.getAgeTerm())
                .userServiceTerm(userCreateRequest.getServiceTerm())
                .userPrivacyTerm(userCreateRequest.getPrivacyTerm())
                .emailMarketingTerm(userCreateRequest.getMarketingTerm())
                .phoneMarketingTerm(userCreateRequest.getMarketingTerm())
                .build();
    }

    private void checkOAuthUser(User user) {
        if(!user.getUserProvider().equals(CATCHER)) {
            throw new BaseException(INVALID_USER_INFO);
        }
    }
}
