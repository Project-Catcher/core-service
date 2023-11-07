package com.catcher.core.dto.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OAuthHistoryResponse {
    private String accessToken;
    private String email;
}
