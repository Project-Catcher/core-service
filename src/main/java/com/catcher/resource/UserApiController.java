package com.catcher.resource;

import com.catcher.core.UserCommandExecutor;
import com.catcher.core.domain.command.Command;
import com.catcher.core.domain.command.UserByUserIdCommand;
import com.catcher.core.domain.response.UserResponseData;
import com.catcher.core.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.catcher.core.domain.entity.User;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserApiController {
    private final UserCommandExecutor userCommandExecutor;
    private final UserService userService;

    @GetMapping("/{userId}")
    public UserResponseData getUserByUserId(@PathVariable String userId) {
        Command<User> command = new UserByUserIdCommand(userService, userId);
        User user = userCommandExecutor.run(command);
        return UserResponseData.from(user);
    }
}
