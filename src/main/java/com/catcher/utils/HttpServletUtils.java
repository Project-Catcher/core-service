package com.catcher.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Optional;

import static java.util.Optional.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpServletUtils {

    public static Optional<String> getHeader(HttpServletRequest request, String name) {
        return ofNullable(request.getHeader(name));
    }

    public static void putHeader(HttpServletResponse response, String name, String value) {
        response.setHeader(name, value);
    }

    public static void addCookie(HttpServletResponse response, String name, String value, int seconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(seconds);
        response.addCookie(cookie);
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        getCookie(request, name).ifPresent(cookie -> {
            cookie.setValue("");
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        });
    }

    public static Optional<String> getCookieValue(HttpServletRequest request, String name) {
        return getCookie(request, name)
                .map(Cookie::getValue);
    }

    public static String getBodyData(HttpServletRequest request) throws IOException {
        try (
                InputStream inputStream = request.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
        ) {
            StringBuilder sb = new StringBuilder();

            char[] buffer = new char[1024];
            int bytesToRead;
            while ((bytesToRead = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, bytesToRead);
            }
            return sb.toString();
        }
    }

    private static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if(cookies != null && cookies.length > 0) {
            return Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals(name))
                    .findAny();
        }

        return empty();
    }
}
