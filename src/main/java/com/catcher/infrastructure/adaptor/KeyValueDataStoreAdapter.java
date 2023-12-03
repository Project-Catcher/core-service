package com.catcher.infrastructure.adaptor;

import com.catcher.common.BaseResponseStatus;
import com.catcher.common.exception.BaseException;
import com.catcher.core.port.KeyValueDataStorePort;
import com.catcher.infrastructure.RedisManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class KeyValueDataStoreAdapter implements KeyValueDataStorePort {

    private final RedisManager redisManager;

    private static final long THREE_MINUTES_AS_MILLISECONDS = 180000L;

    @Override
    public void saveValidationCodeWithKey(final String key, final String authCode) {
        redisManager.putValue(key, authCode, THREE_MINUTES_AS_MILLISECONDS);
    }

    @Override
    public String findValidationCodeWithKey(final String key) {
        return redisManager.getValue(key).orElseThrow(() -> new BaseException(BaseResponseStatus.AUTH_CODE_NOT_FOUND));
    }

    @Override
    public void deleteKey(String key) {
        redisManager.deleteKey(key);
    }

}
