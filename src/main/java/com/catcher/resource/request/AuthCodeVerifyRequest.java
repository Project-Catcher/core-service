package com.catcher.resource.request;


import com.catcher.common.exception.BaseException;
import com.catcher.core.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;

import static com.catcher.common.BaseResponseStatus.CODE_NOT_MATCH;
import static com.catcher.common.BaseResponseStatus.REQUEST_ERROR;

public interface AuthCodeVerifyRequest {

    String getEmail();

    void checkValidation(User user, String answer);

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    class IDAuthCodeVerifyRequest implements AuthCodeVerifyRequest {
        private String email;
        private String authCode;

        @Override
        public void checkValidation(User user, String answer) {
            if (Arrays.asList(email, authCode).contains(null)) {
                throw new BaseException(REQUEST_ERROR);
            } else if (!authCode.equals(answer)) {
                throw new BaseException(CODE_NOT_MATCH);
            } else if (!email.equals(user.getEmail())) {
                throw new BaseException(REQUEST_ERROR);
            }
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    class PWAuthCodeVerifyRequest implements AuthCodeVerifyRequest {
        private String email;
        private String username;
        private String authCode;

        @Override
        public void checkValidation(User user, String answer) {
            if (Arrays.asList(email, authCode, username).contains(null)) {
                throw new BaseException(REQUEST_ERROR);
            } else if (!authCode.equals(answer)) {
                throw new BaseException(CODE_NOT_MATCH);
            } else if (!email.equals(user.getEmail()) || !username.equals(user.getUsername())) {
                throw new BaseException(REQUEST_ERROR);
            }
        }
    }
}