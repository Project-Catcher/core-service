package com.catcher.core;

import com.catcher.core.domain.command.Command;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCommandExecutor implements CommandExecutor{

    @Override
    public <T> T run(Command<T> command) {
        return command.execute();
    }
}
