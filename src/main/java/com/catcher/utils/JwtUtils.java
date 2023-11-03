package com.catcher.utils;

import org.springframework.beans.factory.annotation.Value;

public class JwtUtils {
    public static long ACCESS_TOKEN_EXPIRATION_MILLIS = 1000L * 60 * 60 * 12; // 12Hours
    public static long REFRESH_TOKEN_EXPIRATION_MILLIS = 1000L * 60 * 60 * 24 * 7; // 7Days
}
