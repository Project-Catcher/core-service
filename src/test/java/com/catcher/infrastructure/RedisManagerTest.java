package com.catcher.infrastructure;

import com.catcher.app.AppApplication;
import com.catcher.core.database.DBManager;
import com.catcher.testconfiguriation.EmbeddedRedisConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(classes = {AppApplication.class, EmbeddedRedisConfiguration.class})
class RedisManagerTest {
    @Autowired
    DBManager dbManager;

    @DisplayName("레디스 저장 후, 값이 정상 조회된다.")
    @Test
    void redis_input() {
        //given
        String randomKey = UUID.randomUUID().toString();
        String randomValue = UUID.randomUUID().toString();
        dbManager.putValue(randomKey, randomValue, 1000000);

        //when
        Optional<String> value = dbManager.getValue(randomKey);

        //then
        assertThat(value).isPresent();
        assertThat(value.get()).isEqualTo(randomValue);
    }

    @DisplayName("레디스 저장 후, 삭제하면, 조회시 값이 존재하지 않는다.")
    @Test
    void redis_delete() {
        //given
        String randomKey = UUID.randomUUID().toString();
        String randomValue = UUID.randomUUID().toString();
        dbManager.putValue(randomKey, randomValue, 1000000);
        Optional<String> value = dbManager.getValue(randomKey);
        assertThat(value).isPresent();

        //when
        dbManager.deleteKey(randomKey);

        //then
        assertThat(dbManager.getValue(randomKey)).isEmpty();
    }
}