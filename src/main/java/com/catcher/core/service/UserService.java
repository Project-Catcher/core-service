package com.catcher.core.service;

import com.catcher.core.domain.entity.User;
import com.catcher.datasource.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User findUserByUserId(final String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow();     //TODO: UserNotFoundException throw
    }
}
