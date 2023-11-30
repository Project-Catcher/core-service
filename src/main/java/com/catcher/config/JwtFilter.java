package com.catcher.config;

import com.catcher.common.exception.BaseException;
import com.catcher.core.database.DBManager;
import com.catcher.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.catcher.common.BaseResponseStatus.REDIS_ERROR;
import static com.catcher.utils.HttpServletUtils.getHeader;
import static org.apache.http.HttpHeaders.AUTHORIZATION;

/**
 * 헤더(Authorization)에 있는 토큰을 꺼내 이상이 없는 경우 SecurityContext에 저장
 * Request 이전에 작동
 */

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final DBManager dbManager;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String accessToken = getAccessToken(request);

            if (accessToken != null && jwtTokenProvider.validateToken(accessToken) && !isBlackList(accessToken)) {
                Authentication auth = jwtTokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(auth); // 정상 토큰이면 SecurityContext에 저장
            }
        } catch (RedisConnectionFailureException e) {
            SecurityContextHolder.clearContext();
            throw new BaseException(REDIS_ERROR);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isBlackList(String accessToken) {
        String blackListToken = JwtUtils.generateBlackListToken(accessToken);
        return dbManager.getValue(blackListToken).isPresent();
    }

    private String getAccessToken(HttpServletRequest request) {
        String header = getHeader(request, AUTHORIZATION).orElse(null);

        if (header != null && header.startsWith("Bearer ")) {
            header = header.substring(7);
        }
        return header;
    }
}
