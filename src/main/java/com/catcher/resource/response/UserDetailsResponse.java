package com.catcher.resource.response;

import com.catcher.core.domain.entity.enums.UserGender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsResponse {
    private String nickname;
    private String phone;
    private String email;
    private Date birth;
    private UserGender userGender;
}
