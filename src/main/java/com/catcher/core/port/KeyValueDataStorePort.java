package com.catcher.core.port;

public interface KeyValueDataStorePort {

    void saveValidationCodeWithUserId(String userId, String value);

    String retrieveValidationCodeWithKey(String key);

}
