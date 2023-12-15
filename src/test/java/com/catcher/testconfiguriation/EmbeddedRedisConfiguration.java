package com.catcher.testconfiguriation;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

@TestConfiguration
public class EmbeddedRedisConfiguration {
    private RedisServer redisServer;
    private final static Logger logger = LoggerFactory.getLogger(EmbeddedRedisConfiguration.class);

    public EmbeddedRedisConfiguration(@Value("${spring.data.redis.port}") int port) {
        this.redisServer = new RedisServer(port);
    }

    @PostConstruct
    public void startRedis() {
        try {
            redisServer.start();
        } catch (Exception e) {
            logger.error("error = {}", e.getMessage());
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}
