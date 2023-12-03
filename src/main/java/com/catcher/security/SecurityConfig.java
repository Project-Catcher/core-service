package com.catcher.security;

import com.catcher.common.GlobalExceptionHandlerFilter;
import com.catcher.config.JwtAccessDeniedHandler;
import com.catcher.config.JwtAuthenticationEntryPoint;
import com.catcher.config.JwtTokenProvider;
import com.catcher.core.database.DBManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final DBManager dbManager;
    private final String[] allowedUrls = {
            "/", "/swagger-ui/**", "/users/**", "favicon.ico",
            "/health/**", "/auth/**", "/oauth/**", "/v3/api-docs/**", "/core/v3/api-docs/**"
    };

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailServiceImpl userDetailService, PasswordEncoder passwordEncoder
    ) throws Exception {
        return new CatcherAuthenticationManager(userDetailService, passwordEncoder);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf((csrf) -> csrf.disable())
                .cors(Customizer.withDefaults())
                .headers(headers -> headers.frameOptions(Customizer.withDefaults()))
                .authorizeHttpRequests(request ->
                        request.requestMatchers(allowedUrls).permitAll()
                                .anyRequest().authenticated()
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(
                        (exceptionHandling) ->
                                exceptionHandling
                                        .accessDeniedHandler(jwtAccessDeniedHandler)
                                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .addFilterBefore(new GlobalExceptionHandlerFilter(objectMapper), UsernamePasswordAuthenticationFilter.class)
                .apply(new JwtSecurityConfig(jwtTokenProvider, dbManager));

        return httpSecurity.build();
    }
}
