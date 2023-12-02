package com.catcher.core.port;

public interface KeyValueDataStorePort {

    void saveValidationCodeWithUserId(String key, String value);

    String findValidationCodeWithKey(String key);

    void deleteKey(String key);

}
