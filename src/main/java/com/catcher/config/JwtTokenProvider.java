package com.catcher.config;

import com.catcher.common.exception.BaseException;
import com.catcher.security.UserDetailServiceImpl;
import com.catcher.utils.JwtUtils;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.catcher.common.BaseResponseStatus.EXPIRED_JWT;
import static com.catcher.common.BaseResponseStatus.INVALID_JWT;
import static com.catcher.utils.JwtUtils.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    @Value("${spring.jwt.secret}")
    private String secretKey;
    private final UserDetailServiceImpl userDetailsService;

    public String createAccessToken(Authentication authentication){
        Claims claims = Jwts.claims().setSubject(authentication.getName());
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_MILLIS);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String createRefreshToken(Authentication authentication){
        Claims claims = Jwts.claims().setSubject(authentication.getName());
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_MILLIS);

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

    public boolean validateToken(String token){
        try{
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch(ExpiredJwtException e) {
            log.error(EXPIRED_JWT.getMessage());
            throw new BaseException(EXPIRED_JWT);
        } catch(JwtException e) {
            log.error(INVALID_JWT.getMessage());
            throw new BaseException(INVALID_JWT);
        }
    }
}
