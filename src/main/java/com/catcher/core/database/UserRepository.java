package com.catcher.core.database;

import com.catcher.core.domain.entity.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUsername(final String username);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    User save(User user);
}
