package com.catcher.resource.response;


import com.catcher.core.domain.entity.User;
import com.catcher.core.domain.entity.enums.UserGender;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class UserDetailsResponse {
    private String nickname;
    private String profileImageUrl;
    private String phone;
    private String email;
    private Date birthDate;
    private UserGender gender;

    public static UserDetailsResponse create(User user) {
        return new UserDetailsResponse(
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getPhone(),
                user.getEmail(),
                user.getBirthDate(),
                user.getUserGender()
        );
    }
}
