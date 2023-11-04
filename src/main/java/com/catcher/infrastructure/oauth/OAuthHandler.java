package com.catcher.infrastructure.oauth;

import com.catcher.common.exception.BaseException;
import com.catcher.infrastructure.oauth.properties.OAuthProperties;
import com.catcher.infrastructure.oauth.user.OAuthUserInfo;
import com.catcher.infrastructure.oauth.user.OAuthUserInfoFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static com.catcher.common.BaseResponseStatus.OAUTH_GENERATE_TOKEN_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthHandler extends RestTemplate {

    public OAuthTokenResponse getSignUpToken(OAuthProperties oAuthProperties, Map map) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(oAuthProperties.getSignUpJsonBody(map), httpHeaders);

        try {
            ResponseEntity<OAuthTokenResponse> objectResponseEntity = this.postForEntity(oAuthProperties.getTokenUri(), request, OAuthTokenResponse.class);
            return objectResponseEntity.getBody();
        } catch (HttpClientErrorException e) {
            OAuthTokenResponse oAuthTokenResponse = e.getResponseBodyAs(OAuthTokenResponse.class);
            log.error("OAUTH-{} : error = {}, description = {}",
                    oAuthProperties.getProvider().name(),
                    oAuthTokenResponse.getError(),
                    oAuthTokenResponse.getErrorDescription()
            );
            throw new BaseException(OAUTH_GENERATE_TOKEN_ERROR);
        }
    }

    public OAuthTokenResponse getLoginToken(OAuthProperties oAuthProperties, Map map) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(oAuthProperties.getLoginJsonBody(map), httpHeaders);

        try {
            ResponseEntity<OAuthTokenResponse> objectResponseEntity = this.postForEntity(oAuthProperties.getTokenUri(), request, OAuthTokenResponse.class);
            return objectResponseEntity.getBody();
        } catch (HttpClientErrorException e) {
            OAuthTokenResponse oAuthTokenResponse = e.getResponseBodyAs(OAuthTokenResponse.class);
            log.error("OAUTH-{} : error = {}, description = {}",
                    oAuthProperties.getProvider().name(),
                    oAuthTokenResponse.getError(),
                    oAuthTokenResponse.getErrorDescription()
            );
            throw new BaseException(OAUTH_GENERATE_TOKEN_ERROR);
        }
    }

    public OAuthUserInfo getOAuthUserInfo(OAuthProperties oAuthProperties, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = this.postForEntity(oAuthProperties.getUserInfoUri(), request, Map.class);

        return OAuthUserInfoFactory.getOAuthUserInfo(oAuthProperties.getProvider(), response.getBody());
    }
}
