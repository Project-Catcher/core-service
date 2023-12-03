package com.catcher.core.service;

import cn.apiclub.captcha.Captcha;
import com.catcher.common.BaseResponseStatus;
import com.catcher.common.exception.BaseException;
import com.catcher.core.database.UserRepository;
import com.catcher.core.port.KeyValueDataStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.Objects;

import static com.catcher.utils.KeyGenerator.AuthType;
import static com.catcher.utils.KeyGenerator.generateKey;

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final int WIDTH = 345;
    private static final int HEIGHT = 71;

    private final UserRepository userRepository;

    private final KeyValueDataStorePort keyValueDataStorePort;

    public Captcha generateCaptchaAndSaveAnswer(final String email, AuthType authType) {
        final var user = userRepository.findByEmail(email).orElseThrow(() -> new BaseException(BaseResponseStatus.USERS_NOT_EXISTS));

        Captcha captcha = generateCaptcha();

        final String generatedUserKey = generateKey(user.getId(), authType);
        keyValueDataStorePort.saveValidationCodeWithKey(generatedUserKey, captcha.getAnswer());

        return captcha;
    }

    private Captcha generateCaptcha() {
        return new Captcha.Builder(WIDTH, HEIGHT)
                .addText()
                .addNoise()
                .addBackground()
                .addBorder()
                .build();
    }

    public BufferedImage getImage(Captcha captcha) {
        return captcha.getImage();
    }

    public boolean validateCaptcha(String userEmail, String userAnswer, AuthType authType) {
        final var user = userRepository.findByEmail(userEmail).orElseThrow(() -> new BaseException(BaseResponseStatus.USERS_NOT_EXISTS));

        final String generatedUserKey = generateKey(user.getId(), authType);
        final String answer = keyValueDataStorePort.findValidationCodeWithKey(generatedUserKey);

        boolean isSuccess = Objects.equals(answer, userAnswer);

        if(isSuccess) {
            keyValueDataStorePort.deleteKey(generatedUserKey);
        }

        return isSuccess;
    }

}
