package com.catcher.resource.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CaptchaGenerateRequest {
    public String email;
}
