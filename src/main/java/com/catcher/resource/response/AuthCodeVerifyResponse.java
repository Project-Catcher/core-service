package com.catcher.resource.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthCodeVerifyResponse {
    private Boolean isVerified;
}
