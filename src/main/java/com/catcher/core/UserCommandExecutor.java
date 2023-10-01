package com.catcher.core;

import com.catcher.core.domain.command.Command;
import org.springframework.stereotype.Component;
import com.catcher.core.domain.entity.User;

@Component
public class UserCommandExecutor implements CommandExecutor<User>{

    @Override
    public User run(Command command) {
        //1. datasource layer 호출(DB)
        //2. 가공해서 넘겨줘야 한다
        return switch (command.getClass().getSimpleName()){
            case "UserByUserIdCommand" -> getUserByUserId(command);
            default -> null;
        };
    }

    private User getUserByUserId(Command command){
        //이 부분은 임시
        return new User("kakao_user", "1234", "charles");
    }
}
