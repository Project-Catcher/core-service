package com.catcher.resource.response;

import com.catcher.common.exception.BaseException;
import lombok.Getter;

import static com.catcher.common.BaseResponseStatus.PASSWORD_NOT_MATCH;

@Getter
public class PWChangeRequest {
    private String code;
    private String newPassword;
    private String newPasswordCheck;

    public void checkValidation() {
        if(!newPassword.equals(newPasswordCheck)) {
            throw new BaseException(PASSWORD_NOT_MATCH);
        }
    }
}
