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
public class UserResDto {
    Long id;
    String username;
    String name;
    String email;
    String department;
    String contact;
    String profileUrl;
    UserRole role;

    public static UserResDto from(User user){
        return UserResDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .contact(user.getPhone())
                .profileUrl(user.getProfileImageUrl())
                .role(user.getUserRole())
                .build();
    }

}
