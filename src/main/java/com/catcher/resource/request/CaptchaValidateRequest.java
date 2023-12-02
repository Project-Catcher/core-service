package com.catcher.resource.request;

import lombok.Getter;

@Getter
public class CaptchaValidateRequest {

    private String email;

    private String userAnswer;
}
