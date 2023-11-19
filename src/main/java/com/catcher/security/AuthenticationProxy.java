package com.catcher.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
public class AuthenticationProxy implements AuthenticationManager {
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public CatcherUser authenticate(Authentication authentication) throws AuthenticationException {
        CatcherUser catcherUser = (CatcherUser) userDetailsService.loadUserByUsername((String) authentication.getPrincipal());

        String rawPassword =(String) authentication.getCredentials();
        String encodedPassword = catcherUser.getPassword();
        System.out.println(passwordEncoder.matches(rawPassword, encodedPassword));
        System.out.println(passwordEncoder.matches(encodedPassword,rawPassword));
        if(!passwordEncoder.matches(rawPassword, catcherUser.getPassword())) {
            throw new BadCredentialsException("자격 증명에 실패하였습니다.");
        }

        return catcherUser;
    }
}
