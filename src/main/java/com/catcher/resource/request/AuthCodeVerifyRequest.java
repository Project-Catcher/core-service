package com.catcher.resource.request;

import lombok.Getter;

@Getter
public class AuthCodeVerifyRequest {

    private String email;

    private String authCode;
}
