package com.catcher.config;

import com.catcher.common.exception.BaseException;
import com.catcher.utils.HttpServletUtils;
import io.jsonwebtoken.*;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import static com.catcher.common.BaseResponseStatus.*;
import static com.catcher.utils.HttpServletUtils.addCookie;
import static com.catcher.utils.JwtUtils.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    @Value("${spring.jwt.secret}")
    private String secretKey;
    private final UserDetailsService userDetailsService;

    private AtomicLong atomicLong = new AtomicLong(1L);

    public String createAccessToken(Authentication authentication) {
        Claims claims = Jwts.claims().setSubject(authentication.getName());
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_MILLIS + getRandomSeconds());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String createRefreshToken(Authentication authentication) {
        Claims claims = Jwts.claims().setSubject(authentication.getName());
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_MILLIS + getRandomSeconds());

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        return refreshToken;
    }

    public Authentication getAuthentication(String token) throws BaseException {
        String userPrincipal = Jwts.parser().
                setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody().getSubject();
        UserDetails userDetails = userDetailsService.loadUserByUsername(userPrincipal);

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public boolean validateToken(String token) {
        try {
            checkToken(token);
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error(EXPIRED_JWT.getMessage());
            throw new BaseException(EXPIRED_JWT);
        } catch (JwtException e) {
            log.error(INVALID_JWT.getMessage());
            throw new BaseException(INVALID_JWT);
        }
    }

    private void checkToken(String token) {
        if(StringUtils.isBlank(token)) {
            throw new BaseException(NOT_EXIST_REFRESH_JWT);
        }
    }

    public static void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        addCookie(response, REFRESH_TOKEN_NAME, refreshToken, (int) REFRESH_TOKEN_EXPIRATION_MILLIS);
    }

    public static void removeCookie(HttpServletRequest request, HttpServletResponse response) {
        HttpServletUtils.deleteCookie(request, response, REFRESH_TOKEN_NAME);
    }

    private long getRandomSeconds() {
        return (atomicLong.incrementAndGet() % 10) * 1000;
    }
}
