package com.catcher.core.domain.command;

import com.catcher.core.domain.entity.User;
import com.catcher.core.service.UserService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserByUserIdCommand implements Command<User> {

    private final UserService userService;

    private final String userId;

    @Override
    public User execute() {
        return userService.findUserByUserId(userId);
    }
}
