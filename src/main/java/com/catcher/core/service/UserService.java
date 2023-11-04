package com.catcher.core.service;

import com.catcher.common.exception.BaseException;
import com.catcher.config.JwtTokenProvider;
import com.catcher.core.database.DBManager;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.user.UserCreateRequest;
import com.catcher.core.dto.user.UserCreateResponse;
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
    public UserCreateResponse signUpUser(UserCreateRequest userCreateRequest) {
        validateDuplicateUsername(userCreateRequest.getUsername());
        validateDuplicateEmail(userCreateRequest.getEmail());

        User user = User.builder()
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

        userRepository.save(user);

        return UserCreateResponse.from(user);
    }

    // 유저 중복 확인
    private void validateDuplicateUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            throw new BaseException(USERS_DUPLICATED_USER_NAME);
        }
    }

    private void validateDuplicateEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            throw new BaseException(USERS_DUPLICATED_USER_EMAIL);
        }
    }

    @Transactional
    public TokenDto login(UserLoginRequest userLoginReqDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userLoginReqDto.getUsername(),
                            userLoginReqDto.getPassword()
                    )
            );

            String accessToken = jwtTokenProvider.createAccessToken(authentication);
            String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

            dbManager.putValue(authentication.getName(), refreshToken, REFRESH_TOKEN_EXPIRATION_MILLIS);

            return new TokenDto(accessToken, refreshToken);
        } catch (BadCredentialsException e) {
            log.error(INVALID_USER_PW.getMessage());
            throw new BaseException(INVALID_USER_PW);
        } catch (InternalAuthenticationServiceException e) {
            throw new BaseException(INVALID_USER_NAME);
        }
    }
}
