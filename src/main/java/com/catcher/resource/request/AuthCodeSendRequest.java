package com.catcher.resource.request;

import com.catcher.common.exception.BaseException;
import com.catcher.core.domain.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import static com.catcher.common.BaseResponseStatus.REQUEST_ERROR;

public interface AuthCodeSendRequest {
    String getEmail();

    void checkValidation(User user);

    @Getter
    class IDAuthCodeSendRequest implements AuthCodeSendRequest{
        @NotNull(message = "이메일을 입력해주세요.")
        private String email;

        @Override
        public void checkValidation(User user) {

        }
    }

    @Getter
    class PWAuthCodeSendRequest implements AuthCodeSendRequest{
        @NotNull(message = "이메일을 입력해주세요.")
        private String email;
        @NotNull(message = "아이디를 입력해주세요.")
        private String username;

        @Override
        public void checkValidation(User user) {
            if(!email.equals(user.getEmail()) || !username.equals(user.getUsername())) {
                throw new BaseException(REQUEST_ERROR);
            }
        }
    }
}
