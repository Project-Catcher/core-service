package com.catcher.resource.request;

import com.catcher.common.exception.BaseException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;

import static com.catcher.common.BaseResponseStatus.PASSWORD_NOT_MATCH;
import static com.catcher.common.BaseResponseStatus.REQUEST_ERROR;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PWChangeRequest {
    private String code;
    private String newPassword;
    private String newPasswordCheck;

    public void checkValidation() {
        if (Arrays.asList(code, newPassword, newPasswordCheck).contains(null)) {
            throw new BaseException(REQUEST_ERROR);
        } else if (!newPassword.equals(newPasswordCheck)) {
            throw new BaseException(PASSWORD_NOT_MATCH);
        }
    }
}
