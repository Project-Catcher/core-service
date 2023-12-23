package com.catcher.core.service;

import com.catcher.common.BaseResponseStatus;
import com.catcher.common.exception.BaseException;
import com.catcher.config.JwtTokenProvider;
import com.catcher.core.database.DBManager;
import com.catcher.core.database.UserRepository;
import com.catcher.core.domain.entity.User;
import com.catcher.core.dto.TokenDto;
import com.catcher.core.dto.user.UserCreateRequest;
import com.catcher.core.dto.user.UserInfoResponse;
import com.catcher.core.dto.user.UserLoginRequest;
import com.catcher.infrastructure.external.service.S3UploadService;
import com.catcher.core.dto.user.*;
import com.catcher.resource.external.CatcherFeignController;
import com.catcher.resource.request.PromotionRequest;
import com.catcher.resource.request.UserInfoEditRequest;
import com.catcher.resource.response.UserDetailsResponse;
import com.catcher.security.CatcherUser;
import com.catcher.utils.KeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static com.catcher.common.BaseResponseStatus.*;
import static com.catcher.core.domain.entity.enums.UserProvider.CATCHER;
import static com.catcher.core.domain.entity.enums.UserRole.USER;
import static com.catcher.resource.request.PromotionRequest.PromotionType;
import static com.catcher.utils.JwtUtils.REFRESH_TOKEN_EXPIRATION_MILLIS;
import static com.catcher.utils.KeyGenerator.AuthType.REFRESH_TOKEN;

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
    private final S3UploadService s3UploadService;
    private final CatcherFeignController catcherFeignController;

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

    public boolean checkUsernameExist(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Transactional
    public void signOutUser(User user) {
        user = userRepository.findById(user.getId()).orElseThrow();

        if (user == null) {
            throw new BaseException(USERS_NOT_LOGIN);
        }

        user.signOut();
    }

    @Transactional
    public void togglePhonePromotion(User user, PromotionRequest promotionRequest, PromotionType type) {
        user = userRepository.findById(user.getId()).orElseThrow();
        switch (type) {
            case PHONE -> user.changePhoneTerm(promotionRequest.getIsOn());
            case EMAIL -> user.changeEmailTerm(promotionRequest.getIsOn());
        }
    }

    public UserInfoResponse getMyInfo(User user) {
        return new UserInfoResponse(user.getUsername(),
                user.getPhone(),
                user.getEmail(),
                user.getProfileImageUrl(),
                user.getNickname(),
                user.getEmailMarketingTerm(),
                user.getPhoneMarketingTerm());
    }

    public UserDetailsResponse getDetailsInfo(User user) {
        return new UserDetailsResponse(
                user.getNickname(),
                user.getPhone(),
                user.getEmail(),
                user.getBirthDate(),
                user.getUserGender()
        );
    }

    @Transactional
    public void editUserInfo(User user,
                             MultipartFile profileFile,
                             UserInfoEditRequest userInfoEditRequest) {
        user = userRepository.findById(user.getId()).orElseThrow();
        if (!StringUtils.equals(user.getNickname(), userInfoEditRequest.getNickname())) {
            Optional<User> optionalUser = userRepository.findByNickname(userInfoEditRequest.getNickname());
            if (optionalUser.isPresent()) {
                throw new BaseException(USERS_DUPLICATED_NICKNAME);
            }
        }

        user.changeMyInfo(userInfoEditRequest.getNickname(),
                userInfoEditRequest.getGender(),
                userInfoEditRequest.getBirth()
        );

        if (profileFile != null && !profileFile.isEmpty()) {
            String fileName = s3UploadService.uploadFile(profileFile);
            user.changeProfileUrl(fileName);
        }
    }

    @Transactional
    public void updateAdditionalInfo(User user, UserAdditionalInfoRequest request, String token) {
        user = userRepository.findById(user.getId()).orElseThrow();
        user.changeIntroduceContent(request.getIntroduceContent());

        catcherFeignController.changeUserTags(new UserTagsEdit(request.getTags()), token);
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
                .phoneAuthentication(userCreateRequest.getMarketingTerm())
                .userAgeTerm(userCreateRequest.getAgeTerm())
                .userServiceTerm(userCreateRequest.getServiceTerm())
                .userPrivacyTerm(userCreateRequest.getPrivacyTerm())
                .emailMarketingTerm(userCreateRequest.getMarketingTerm())
                .phoneMarketingTerm(userCreateRequest.getMarketingTerm())
                .build();
    }

    private void checkOAuthUser(User user) {
        if (!user.getUserProvider().equals(CATCHER)) {
            throw new BaseException(INVALID_USER_INFO);
        }
    }
}
