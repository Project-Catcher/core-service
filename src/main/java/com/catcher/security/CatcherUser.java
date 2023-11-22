package com.catcher.security;

import com.catcher.core.domain.entity.User;
import com.catcher.core.domain.entity.enums.UserRole;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

import static java.util.Collections.singleton;

/**
 * SecurityContext에 들어갈 Authentication 정보
 */
@Getter
public class CatcherUser extends org.springframework.security.core.userdetails.User implements Authentication {
    private User user;
    public CatcherUser(User user) {
        super(user.getUsername(), user.getPassword(), parseAuthority(user.getUserRole()));
        this.user = user;
    }

    private static Collection<? extends GrantedAuthority> parseAuthority(UserRole userRole) {
        return singleton(new SimpleGrantedAuthority(userRole.getValue()));
    }

    @Override
    public Object getCredentials() {
        return this.user;
    }

    @Override
    public Object getDetails() {
        return user.getUsername();
    }

    @Override
    public Object getPrincipal() {
        return user.getPassword();
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

    }

    @Override
    public String getName() {
        return user.getUsername();
    }
}
