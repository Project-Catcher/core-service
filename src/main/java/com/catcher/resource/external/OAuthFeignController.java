package com.catcher.resource.external;

import com.catcher.infrastructure.oauth.OAuthTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@FeignClient(name = "oauth", url = "runtime")
public interface OAuthFeignController {

    @GetMapping
    OAuthTokenResponse getWithRequestParams(URI uri, @RequestParam Map params);

    @PostMapping
    Map postWithAuthorizationHeader(URI uri, @RequestHeader("Authorization") String accessToken);

    @PostMapping
    Map postWithBody(URI uri, @RequestBody Object body);
}
