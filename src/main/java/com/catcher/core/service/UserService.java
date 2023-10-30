package com.catcher.core.service;

import com.catcher.common.exception.BaseException;
import com.catcher.config.JwtTokenProvider;
import com.catcher.core.domain.entity.enums.UserRole;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.user.UserCreateRequest;
import com.catcher.core.dto.user.UserCreateResponse;
import com.catcher.core.dto.user.UserLoginRequest;
import com.catcher.core.dto.user.UserResponse;
import com.catcher.core.domain.entity.User;
import com.catcher.datasource.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.catcher.common.BaseResponseStatus.*;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
@Log4j2
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public UserCreateResponse signUpUser(UserCreateRequest userCreateRequest) throws BaseException {
        validateDuplicateUser(userCreateRequest.getName());

        User user = User.builder()
                .name(userCreateRequest.getName())
                .password(passwordEncoder.encode(userCreateRequest.getPassword()))
                .role(userCreateRequest.getRole() == 0 ? UserRole.ADMIN : UserRole.USER)
                .username(userCreateRequest.getUsername())
                .email(userCreateRequest.getEmail())
                .phone(userCreateRequest.getPhone())
                .nickname(userCreateRequest.getNickname())
                .profileImageUrl(null)
                .userAgeTerm(userCreateRequest.getAgeTerm())
                .userServiceTerm(userCreateRequest.getServiceTerm())
                .userPrivacyTerm(userCreateRequest.getPrivacyTerm())
                .userLocationTerm(userCreateRequest.getLocationTerm())
                .userMarketingTerm(userCreateRequest.getMarketingTerm())
                .introduceContent(userCreateRequest.getIntroduceContent())
                .build();

        userRepository.save(user);

        return UserCreateResponse.from(user);
    }


    // 유저 중복 확인
    private void validateDuplicateUser(String username) throws BaseException {
        Optional<User> findUsers = userRepository.findByUsername(username);
        if (!findUsers.isEmpty()){
            throw new BaseException(USERS_DUPLICATED_USER_NAME);
        }
    }

    @Transactional
    public TokenDto login(UserLoginRequest userLoginReqDto) throws BaseException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userLoginReqDto.getUsername(),
                            userLoginReqDto.getPassword()
                    )
            );

            TokenDto tokenDto = new TokenDto(
                    jwtTokenProvider.createAccessToken(authentication),
                    jwtTokenProvider.createRefreshToken(authentication)
            );

            return tokenDto;

        }catch(BadCredentialsException e){
            log.error(INVALID_USER_PW.getMessage());
            throw new BaseException(INVALID_USER_PW);
        }
    }

    public UserResponse getUser(Long id) {
        Optional<User> users = userRepository.findById(id);
        User user = users.orElseThrow(() -> {
            log.error(INVALID_USER_NAME.getMessage());
            return new BaseException(INVALID_USER_NAME);
        });

        return UserResponse.from(user);
    }
}
