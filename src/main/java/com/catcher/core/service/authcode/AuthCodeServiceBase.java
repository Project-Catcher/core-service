package com.catcher.core.service.authcode;

import com.catcher.common.BaseResponseStatus;
import com.catcher.common.exception.BaseException;
import com.catcher.core.database.UserRepository;
import com.catcher.core.domain.entity.User;
import com.catcher.core.port.KeyValueDataStorePort;
import com.catcher.resource.request.AuthCodeSendRequest;
import com.catcher.resource.request.AuthCodeVerifyRequest;
import com.catcher.resource.response.AuthCodeVerifyResponse;
import com.catcher.resource.request.PWChangeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

import static com.catcher.utils.KeyGenerator.AuthType;
import static com.catcher.utils.KeyGenerator.generateKey;

@RequiredArgsConstructor
public abstract class AuthCodeServiceBase {
    protected final KeyValueDataStorePort keyValueDataStorePort;
    protected final UserRepository userRepository;

    @Transactional
    public void changePassword(PWChangeRequest pwChangeRequest) {
        throw new UnsupportedOperationException();
    }

    public String generateAndSaveRandomKey(final AuthCodeSendRequest authCodeSendRequest) {
        final var user = userRepository.findByEmail(authCodeSendRequest.getEmail()).orElseThrow(() -> new BaseException(BaseResponseStatus.USERS_NOT_EXISTS));
        authCodeSendRequest.checkValidation(user);

        final var generatedKey = String.valueOf(generateSixDigitsRandomCode());
        final var generatedDataStoreKey = generateKey(user.getId(), getAuthType());
        keyValueDataStorePort.saveValidationCodeWithKey(generatedDataStoreKey, generatedKey);

        return generatedKey;
    }

    public AuthCodeVerifyResponse verifyAuthCode(AuthCodeVerifyRequest request) {
        final var user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new BaseException(BaseResponseStatus.USERS_NOT_EXISTS));
        final var generatedDataStoreKey = generateKey(user.getId(), getAuthType());
        final String storedAuthCode = keyValueDataStorePort.findValidationCodeWithKey(generatedDataStoreKey);

        request.checkValidation(user, storedAuthCode);
        keyValueDataStorePort.deleteKey(generatedDataStoreKey);

        return createAuthCodeVerifyResponse(user);
    }

    protected abstract AuthCodeVerifyResponse createAuthCodeVerifyResponse(User user);

    public boolean support(AuthType authType) {
        return getAuthType().equals(authType);
    }

    private int generateSixDigitsRandomCode() {
        int min = 100000;
        int max = 999999;
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    protected abstract AuthType getAuthType();
}
