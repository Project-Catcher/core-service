package com.catcher.core.domain.command;

import lombok.Data;

@Data
public class UserByUserIdCommand implements Command {
    String userId;

    public UserByUserIdCommand(String userId) {
        this.userId = userId;
    }
}
