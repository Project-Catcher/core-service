package com.catcher.core.dto.user;

import com.catcher.core.domain.entity.User;
import com.catcher.core.domain.entity.enums.UserRole;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserCreateResponse {
    String name;
    String username;
    String email;
    String department;
    String contact;
    UserRole role;

    public static UserCreateResponse from(User user){
        return UserCreateResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .contact(user.getPhone())
                .role(user.getUserRole())
                .build();
    }
}