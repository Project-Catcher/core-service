package com.catcher.resource;

import com.catcher.core.UserCommandExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserApiController {
    private final UserCommandExecutor userCommandExecutor;

}
