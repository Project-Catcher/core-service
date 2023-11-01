package com.catcher.core.dto.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthTokenResponse { // TODO : Apply name properties
    private String token_type;
    private String access_token;
    private String expires_in;
    private String refresh_token;
}
