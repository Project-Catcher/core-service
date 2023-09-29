package com.catcher.core;

import com.catcher.core.domain.command.Command;

public interface CommandExecutor<T> {
    T run(Command command);
}
