package com.catcher.core.service;

import com.catcher.common.exception.BaseException;
import com.catcher.config.JwtTokenProvider;
import com.catcher.core.domain.entity.enums.UserRole;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.user.UserCreateReqDto;
import com.catcher.core.dto.user.UserCreateResDto;
import com.catcher.core.dto.user.UserLoginReqDto;
import com.catcher.core.dto.user.UserResDto;
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
    public UserCreateResDto signUpUser(UserCreateReqDto userCreateReqDto) throws BaseException {
        validateDuplicateUser(userCreateReqDto.getName());

        User user = User.builder()
                .name(userCreateReqDto.getName())
                .password(passwordEncoder.encode(userCreateReqDto.getPassword()))
                .role(userCreateReqDto.getRole() == 0 ? UserRole.ADMIN : UserRole.USER)
                .username(userCreateReqDto.getUsername())
                .email(userCreateReqDto.getEmail())
                .phone(userCreateReqDto.getPhone())
                .nickname(userCreateReqDto.getNickname())
                .profileImageUrl(null)
                .userAgeTerm(userCreateReqDto.getAgeTerm())
                .userServiceTerm(userCreateReqDto.getServiceTerm())
                .userPrivacyTerm(userCreateReqDto.getPrivacyTerm())
                .userLocationTerm(userCreateReqDto.getLocationTerm())
                .userMarketingTerm(userCreateReqDto.getMarketingTerm())
                .introduceContent(userCreateReqDto.getIntroduceContent())
                .build();

        userRepository.save(user);

        return UserCreateResDto.from(user);
    }


    // 유저 중복 확인
    private void validateDuplicateUser(String uid) throws BaseException {
        Optional<User> findUsers = userRepository.findByUid(uid);
        if (!findUsers.isEmpty()){
            throw new BaseException(USERS_DUPLICATED_USER_ID);
        }
    }

    @Transactional
    public TokenDto login(UserLoginReqDto userLoginReqDto) throws BaseException {
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

    public UserResDto getUser(String uid) {
        Optional<User> users = userRepository.findByUid(uid);
        User user = users.orElseThrow(() -> {
            log.error(INVALID_USER_UID.getMessage());
            return new BaseException(INVALID_USER_UID);
        });

        return UserResDto.from(user);
    }
}
