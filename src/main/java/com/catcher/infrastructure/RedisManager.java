package com.catcher.infrastructure;

import com.catcher.core.database.DBManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

import static java.time.Duration.*;
import static java.util.Optional.*;

@Service
@RequiredArgsConstructor
public class RedisManager implements DBManager {
    private final StringRedisTemplate redisTemplate;

    public void putValue(String key, String value, long milliseconds) {
        redisTemplate.opsForValue().set(key, value, ofMillis(milliseconds));
    }

    public Optional<String> getValue(String key) {
        return ofNullable(redisTemplate.opsForValue().get(key));
    }

    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }
}
