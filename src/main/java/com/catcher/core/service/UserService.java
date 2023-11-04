package com.catcher.core.service;

import com.catcher.common.exception.BaseException;
import com.catcher.config.JwtTokenProvider;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.user.UserCreateRequest;
import com.catcher.core.dto.user.UserCreateResponse;
import com.catcher.core.dto.user.UserLoginRequest;
import com.catcher.core.domain.entity.User;
import com.catcher.core.database.UserRepository;
import com.catcher.infrastructure.RedisManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
import static com.catcher.utils.JwtUtils.*;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
@Log4j2
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final RedisManager redisManager;

    @Transactional
    public UserCreateResponse signUpUser(UserCreateRequest userCreateRequest) {
        validateDuplicateUsername(userCreateRequest.getName());
        validateDuplicateEmail(userCreateRequest.getEmail());

        User user = User.builder()
                .name(userCreateRequest.getName())
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
            throw new BaseException(USERS_DUPLICATED_USER_NAME);
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

            redisManager.putValue(authentication.getName(), refreshToken, REFRESH_TOKEN_EXPIRATION_MILLIS);

            return new TokenDto(accessToken, refreshToken);
        } catch (BadCredentialsException e) {
            log.error(INVALID_USER_PW.getMessage());
            throw new BaseException(INVALID_USER_PW);
        } catch (InternalAuthenticationServiceException e) {
            throw new BaseException(INVALID_USER_NAME);
        }
    }
}
