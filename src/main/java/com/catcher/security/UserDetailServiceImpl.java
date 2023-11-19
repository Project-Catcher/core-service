package com.catcher.security;

import com.catcher.common.exception.BaseException;
import com.catcher.core.database.UserRepository;
import com.catcher.core.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.catcher.common.BaseResponseStatus.INVALID_USER_NAME;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public CatcherUser loadUserByUsername(String username) throws BaseException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error(INVALID_USER_NAME.getMessage());
                    return new BaseException(INVALID_USER_NAME);
                });


        return new CatcherUser(user);
    }
}
