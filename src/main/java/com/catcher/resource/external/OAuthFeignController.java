package com.catcher.resource.external;

import com.catcher.infrastructure.oauth.OAuthTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.util.Map;

@FeignClient(name = "test", url = "runtime")
public interface OAuthFeignController {

    @GetMapping
    OAuthTokenResponse getWithParams(URI url, @RequestParam Map params);

    @PostMapping
    Map postWithParams(URI url, @RequestHeader("Authorization") String accessToken);
}
