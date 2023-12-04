package com.catcher.resource.request;


import com.catcher.common.exception.BaseException;
import com.catcher.core.domain.entity.User;
import lombok.Getter;

import static com.catcher.common.BaseResponseStatus.CODE_NOT_MATCH;
import static com.catcher.common.BaseResponseStatus.REQUEST_ERROR;
import static com.catcher.utils.KeyGenerator.AuthType;
import static com.catcher.utils.KeyGenerator.AuthType.FIND_ID;
import static com.catcher.utils.KeyGenerator.AuthType.FIND_PASSWORD;

public interface AuthCodeVerifyRequest {

    String getEmail();

    String getAuthCode();

    void checkValidation(User user, String answer);

    AuthType getAuthType();

    @Getter
    class IDAuthCodeVerifyRequest implements AuthCodeVerifyRequest {
        private String email;
        private String authCode;

        @Override
        public void checkValidation(User user, String answer) {
            if (!authCode.equals(answer)) {
                throw new BaseException(CODE_NOT_MATCH);
            } else if (!email.equals(user.getEmail())) {
                throw new BaseException(REQUEST_ERROR);
            }
        }

        @Override
        public AuthType getAuthType() {
            return FIND_ID;
        }
    }

    @Getter
    class PWAuthCodeVerifyRequest implements AuthCodeVerifyRequest {
        private String email;
        private String username;
        private String authCode;

        @Override
        public void checkValidation(User user, String answer) {
            if (!authCode.equals(answer)) {
                throw new BaseException(CODE_NOT_MATCH);
            } else if (!email.equals(user.getEmail()) || !username.equals(user.getUsername())) {
                throw new BaseException(REQUEST_ERROR);
            }
        }

        @Override
        public AuthType getAuthType() {
            return FIND_PASSWORD;
        }
    }
}