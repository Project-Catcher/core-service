package com.catcher.core.dto.user;

import com.catcher.core.domain.entity.User;
import com.catcher.core.domain.entity.enums.UserRole;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserCreateResDto {
    String name;
    String username;
    String email;
    String department;
    String contact;
    UserRole role;

    public static UserCreateResDto from(User user){
        return UserCreateResDto.builder()
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .contact(user.getPhone())
                .role(user.getUserRole())
                .build();
    }
}