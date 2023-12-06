package com.catcher.core.dto.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserInfoResponse {
    private String username;
    private String phone;
    private String email;
    private String profileImageUrl;
    private String nickname;
    private ZonedDateTime emailMarketingTerm;
    private ZonedDateTime phoneMarketingTerm;
}
