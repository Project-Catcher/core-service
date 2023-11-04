package com.catcher.core.dto.user;

import com.catcher.core.domain.entity.User;
import com.catcher.core.domain.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    Long id;
    String username;
    String email;
    String department;
    String contact;
    String profileUrl;
    UserRole role;

    public static UserResponse from(User user){
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .contact(user.getPhone())
                .profileUrl(user.getProfileImageUrl())
                .role(user.getUserRole())
                .build();
    }
}
