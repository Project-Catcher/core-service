package com.catcher.security;

import com.catcher.common.exception.BaseException;
import com.catcher.core.domain.entity.User;
import com.catcher.datasource.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import static com.catcher.common.BaseResponseStatus.INVALID_USER_NAME;

@Service
@Log4j2
public class UserDetailServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws BaseException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error(INVALID_USER_NAME.getMessage());
                    return new BaseException(INVALID_USER_NAME);
                });

        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        return new org
                .springframework
                .security
                .core
                .userdetails
                .User(user.getUsername(), user.getPassword(), grantedAuthorities);
    }
}