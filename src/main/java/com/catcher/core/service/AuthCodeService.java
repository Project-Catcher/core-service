package com.catcher.core.service;

import com.catcher.common.BaseResponseStatus;
import com.catcher.common.exception.BaseException;
import com.catcher.core.database.UserRepository;
import com.catcher.core.port.KeyValueDataStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthCodeService {

    private final KeyValueDataStorePort keyValueDataStorePort;

    private final UserRepository userRepository;

    public int generateSixDigitsRandomCode() {
        int min = 100000;
        int max = 999999;
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    public String generateAndSaveRandomKey(final String email) {
        final var user = userRepository.findByEmail(email).orElseThrow(() -> new BaseException(BaseResponseStatus.USERS_NOT_EXISTS));
        final var generatedKey = String.valueOf(generateSixDigitsRandomCode());
        final var generatedDataStoreKey = generateDataStoreKey(user.getId());
        keyValueDataStorePort.saveValidationCodeWithUserId(generatedDataStoreKey, generatedKey);

        return generatedKey;
    }

    public boolean verifyAuthCode(final String email, String authCode) {
        final var user = userRepository.findByEmail(email).orElseThrow(() -> new BaseException(BaseResponseStatus.USERS_NOT_EXISTS));
        final var generatedDataStoreKey = generateDataStoreKey(user.getId());
        final String storedAuthCode = keyValueDataStorePort.retrieveValidationCodeWithKey(generatedDataStoreKey);

        return authCode.equals(storedAuthCode);
    }
    private String generateDataStoreKey(final Long userId) {
        return String.format("%s_%s", userId, "AUTHCODE");
    }

}
