package com.catcher.common.exception;

import com.catcher.common.BaseResponseStatus;
import lombok.Getter;

@Getter
public class OAuthException extends BaseException {
    private String accessToken;

    public OAuthException(BaseResponseStatus status, String accessToken) {
        super(status);
        this.accessToken = accessToken;
    }
}
