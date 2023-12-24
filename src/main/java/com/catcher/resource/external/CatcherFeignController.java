package com.catcher.resource.external;

import com.catcher.core.dto.user.UserTagsEdit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "catcher-service", url = "${url.catcher-service}")
public interface CatcherFeignController {
    @PutMapping("/user-tag")
    void changeUserTags(@RequestBody UserTagsEdit userTagsEdit,
                        @RequestHeader(AUTHORIZATION) String accessToken);
}
