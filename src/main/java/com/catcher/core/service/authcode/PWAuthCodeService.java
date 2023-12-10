package com.catcher.core.service.authcode;

import com.catcher.common.exception.BaseException;
import com.catcher.core.database.UserRepository;
import com.catcher.core.domain.entity.User;
import com.catcher.core.port.KeyValueDataStorePort;
import com.catcher.resource.response.AuthCodeVerifyResponse;
import com.catcher.resource.request.PWChangeRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.catcher.common.BaseResponseStatus.EXPIRED_CODE;
import static com.catcher.resource.response.AuthCodeVerifyResponse.PWAuthCodeVerifyResponse;
import static com.catcher.utils.KeyGenerator.AuthType;
import static com.catcher.utils.KeyGenerator.AuthType.FIND_PASSWORD;
import static com.catcher.utils.KeyGenerator.AuthType.FIND_PASSWORD_SUCCESS;
import static com.catcher.utils.KeyGenerator.generateKey;

@Service
public class PWAuthCodeService extends AuthCodeServiceBase {
    private final PasswordEncoder passwordEncoder;

    public PWAuthCodeService(KeyValueDataStorePort keyValueDataStorePort, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        super(keyValueDataStorePort, userRepository);
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void changePassword(PWChangeRequest pwChangeRequest) {
        pwChangeRequest.checkValidation();
        String key = generateKey(pwChangeRequest.getCode(), FIND_PASSWORD_SUCCESS);
        String email;

        try {
            email = keyValueDataStorePort.findValidationCodeWithKey(key);
        } catch (BaseException e) {
            throw new BaseException(EXPIRED_CODE);
        }

        User user = userRepository.findByEmail(email).orElseThrow();
        String encodedNewPassword = passwordEncoder.encode(pwChangeRequest.getNewPassword());
        user.changePassword(encodedNewPassword);
    }

    @Override
    protected AuthCodeVerifyResponse createAuthCodeVerifyResponse(User user) {
        String code = UUID.randomUUID().toString();
        String key = generateKey(code, FIND_PASSWORD_SUCCESS);
        keyValueDataStorePort.saveValidationCodeWithKey(key, user.getEmail());

        return new PWAuthCodeVerifyResponse(code);
    }

    @Override
    protected AuthType getAuthType() {
        return FIND_PASSWORD;
    }
}
