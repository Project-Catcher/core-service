package com.catcher.core;

import com.catcher.core.domain.User;
import com.catcher.core.domain.command.Command;
import com.catcher.core.domain.command.UserByUserIdCommand;
import org.springframework.stereotype.Component;

@Component
public class UserCommandExecutor implements CommandExecutor<User>{

    @Override
    public User run(Command command) {
        //1. datasource layer 호출(DB)
        //2. 가공해서 넘겨줘야 한다
        return switch (command) {
            case UserByUserIdCommand userByUserIdCommand -> getUserByUserId(userByUserIdCommand);
            default -> null;
        };
    }

    private User getUserByUserId(UserByUserIdCommand command){
        //이 부분은 임시
        final String userId = command.getUserId();
        return new User(userId, "1234", "charles");
    }
}
