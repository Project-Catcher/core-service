package com.catcher.core.port;

public interface KeyValueDataStorePort {

    void saveAuthCodeWithUserId(String userId, String value);

    String retrieveAuthCodeWithUserId(String userId);

}
