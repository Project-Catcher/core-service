package com.catcher.utils;

public class JwtUtils {
    public static long ACCESS_TOKEN_EXPIRATION_MILLIS = 1000L * 60 * 60 * 12; // 12Hours
    public static long REFRESH_TOKEN_EXPIRATION_MILLIS = 1000L * 60 * 60 * 24 * 7; // 7Days

    public final static String REFRESH_TOKEN_NAME = "RefreshToken";

}
