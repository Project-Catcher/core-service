package com.catcher.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
}
