package com.catcher.config;

import com.catcher.common.exception.BaseException;
import com.catcher.utils.HttpServletUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.HttpHeaders;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

import static com.catcher.common.BaseResponseStatus.INVALID_JWT;
import static com.catcher.common.BaseResponseStatus.REDIS_ERROR;
import static com.catcher.utils.HttpServletUtils.*;
import static org.apache.http.HttpHeaders.*;

/**
 * 헤더(Authorization)에 있는 토큰을 꺼내 이상이 없는 경우 SecurityContext에 저장
 * Request 이전에 작동
 */

public class JwtFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    public JwtFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String accessToken = getAccessToken(request);

        try {
            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
                Authentication auth = jwtTokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(auth); // 정상 토큰이면 SecurityContext에 저장
            }
        } catch (RedisConnectionFailureException e) {
            SecurityContextHolder.clearContext();
            throw new BaseException(REDIS_ERROR);
        } catch (Exception e) {
            throw new BaseException(INVALID_JWT);
        }

        filterChain.doFilter(request, response);
    }

    private String getAccessToken(HttpServletRequest request) {
        String header = getHeader(request, AUTHORIZATION).orElse(null);

        if (header != null && header.startsWith("Bearer ")) {
            header = header.substring(7);
        }
        return header;
    }
}
