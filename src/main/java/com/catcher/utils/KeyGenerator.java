package com.catcher.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KeyGenerator {

    public static String generateKey(Object obj, AuthType type) {
        return String.format("%s:%s", type.name(), obj);
    }

    public enum AuthType {
        BLACK_LIST_ACCESS_TOKEN,
        REFRESH_TOKEN,
        FIND_ID,
        FIND_PASSWORD,
        FIND_PASSWORD_SUCCESS,
        CAPTCHA_ID,
        CAPTCHA_PASSWORD,
    }
}
