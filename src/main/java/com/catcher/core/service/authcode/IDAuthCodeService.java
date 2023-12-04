package com.catcher.core.service.authcode;

import com.catcher.core.database.UserRepository;
import com.catcher.core.domain.entity.User;
import com.catcher.core.port.KeyValueDataStorePort;
import com.catcher.resource.response.AuthCodeVerifyResponse;
import org.springframework.stereotype.Service;

import java.sql.Date;

import static com.catcher.resource.response.AuthCodeVerifyResponse.*;
import static com.catcher.utils.KeyGenerator.AuthType;
import static com.catcher.utils.KeyGenerator.AuthType.FIND_ID;

@Service
public class IDAuthCodeService extends AuthCodeServiceBase {

    public IDAuthCodeService(KeyValueDataStorePort keyValueDataStorePort, UserRepository userRepository) {
        super(keyValueDataStorePort, userRepository);
    }

    @Override
    protected AuthCodeVerifyResponse createAuthCodeVerifyResponse(User user) {
        return new IDAuthCodeVerifyResponse(user.getUsername(), Date.from(user.getCreatedAt().toInstant()));
    }

    @Override
    protected AuthType getAuthType() {
        return FIND_ID;
    }
}
