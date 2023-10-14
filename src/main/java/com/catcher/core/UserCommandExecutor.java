package com.catcher.core;

import com.catcher.datasource.UserRepository;
import com.catcher.core.domain.command.Command;
import com.catcher.core.domain.command.UserByUserIdCommand;
import com.catcher.core.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCommandExecutor implements CommandExecutor<User>{

    private final UserRepository userRepository;

    @Override
    public User run(Command command) {
        //1. datasource layer 호출(DB)
        //2. 가공해서 넘겨줘야 한다
        return switch (command.getClass().getSimpleName()) {
            case "UserByUserIdCommand" -> getUserByUserId(command);
            default -> null;
        };
    }

    private User getUserByUserId(Command command) {

        final UserByUserIdCommand userByUserIdCommand = (UserByUserIdCommand) command;
        return userRepository
                .findByUsername(userByUserIdCommand.getUserId())
                .orElseThrow(); // TODO: fill custom Exception
    }
}
