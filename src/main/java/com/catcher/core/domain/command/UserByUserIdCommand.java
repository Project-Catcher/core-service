package com.catcher.core.domain.command;

public class UserByUserIdCommand implements Command{
    String userId;

    public UserByUserIdCommand(String userId){
        this.userId = userId;
    }
}
