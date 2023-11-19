package com.catcher.core.service;

import com.catcher.common.BaseResponseStatus;
import com.catcher.common.exception.BaseException;
import com.catcher.config.JwtTokenProvider;
import com.catcher.core.database.DBManager;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.user.UserCreateRequest;
import com.catcher.core.dto.user.UserLoginRequest;
import com.catcher.core.domain.entity.User;
import com.catcher.core.database.UserRepository;
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

import java.util.Optional;

import static com.catcher.common.BaseResponseStatus.*;
import static com.catcher.core.domain.entity.enums.UserProvider.*;
import static com.catcher.core.domain.entity.enums.UserRole.*;
import static com.catcher.utils.JwtUtils.*;

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

    private TokenDto checkAuthenticationAndGetTokenDto(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
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

    private void checkDuplicateUser(Optional<User> optionalUser, BaseResponseStatus responseStatus) {
        if (optionalUser.isPresent()) {
            throw new BaseException(responseStatus);
        }
    }

    private User createUser(UserCreateRequest userCreateRequest) {
        return User.builder()
                .password(passwordEncoder.encode(userCreateRequest.getPassword()))
                .username(userCreateRequest.getUsername())
                .email(userCreateRequest.getEmail())
                .phone(userCreateRequest.getPhone())
                .nickname(userCreateRequest.getNickname())
                .userAgeTerm(userCreateRequest.getAgeTerm())
                .userServiceTerm(userCreateRequest.getServiceTerm())
                .userPrivacyTerm(userCreateRequest.getPrivacyTerm())
                .userLocationTerm(userCreateRequest.getLocationTerm())
                .userMarketingTerm(userCreateRequest.getMarketingTerm())
                .role(USER)
                .userProvider(CATCHER)
                .build();
    }
}
