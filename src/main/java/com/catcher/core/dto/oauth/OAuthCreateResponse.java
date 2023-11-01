package com.catcher.core.dto.oauth;

import com.catcher.core.domain.entity.User;
import com.catcher.core.domain.entity.enums.UserRole;
import com.catcher.core.dto.user.UserCreateResponse;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OAuthCreateResponse {
    String name;
    String username;
    String email;
    String department;
    String contact;
    UserRole role;

    public static OAuthCreateResponse from(User user){
        return OAuthCreateResponse.builder()
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .contact(user.getPhone())
                .role(user.getUserRole())
                .build();
    }
}
