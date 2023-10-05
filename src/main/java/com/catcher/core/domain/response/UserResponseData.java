package com.catcher.core.domain.response;

import lombok.Builder;
import lombok.Getter;
import com.catcher.core.domain.entity.User;

@Getter
public class UserResponseData {
    private Long id;
    private String userId;
    private String password;
    private String name;

    @Builder
    public UserResponseData(Long id, String userId, String password, String name) {
        this.id = id;
        this.userId = userId;
        this.password = password;
        this.name = name;
    }

    public static UserResponseData from(User user) {
        return UserResponseData.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .password(user.getPassword())
                .name(user.getName())
                .build();
    }
}
