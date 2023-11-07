package com.catcher.security;

import com.catcher.common.exception.BaseException;
import com.catcher.core.domain.entity.User;
import com.catcher.core.domain.entity.enums.UserRole;
import com.catcher.core.database.UserRepository;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;

import static com.catcher.common.BaseResponseStatus.INVALID_USER_NAME;

@Service
@Slf4j
public class UserDetailServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws BaseException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error(INVALID_USER_NAME.getMessage());
                    return new BaseException(INVALID_USER_NAME);
                });

        Collection<? extends GrantedAuthority> authorities = createAuthorities(user.getUserRole());
        return new org
                .springframework
                .security
                .core
                .userdetails
                .User(user.getUsername(), user.getPassword(), authorities);
    }

    private Collection<? extends GrantedAuthority> createAuthorities(UserRole userRole) {
        return Set.of(new SimpleGrantedAuthority(userRole.getValue()));
    }
}
