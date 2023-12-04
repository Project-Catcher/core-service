package com.catcher.resource.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

public interface AuthCodeVerifyResponse {

    @Getter
    @AllArgsConstructor
    class IDAuthCodeVerifyResponse implements AuthCodeVerifyResponse{
        private String username;
        private Date createdAt;
    }

    @Getter
    @AllArgsConstructor
    class PWAuthCodeVerifyResponse implements AuthCodeVerifyResponse{
        private String code;
    }

}