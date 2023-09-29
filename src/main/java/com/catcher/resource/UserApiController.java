package com.catcher.resource;

import com.catcher.core.UserCommandExecutor;
import com.catcher.core.domain.command.UserByUserIdCommand;
import com.catcher.core.domain.response.UserResponseData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.catcher.core.domain.User;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserApiController {
    private final UserCommandExecutor userCommandExecutor;

    @GetMapping("/{userId}")
    public UserResponseData getUserByUserId(@PathVariable String userId){
        User user = userCommandExecutor.run(new UserByUserIdCommand(userId));
        return UserResponseData.from(user);
    }
}
