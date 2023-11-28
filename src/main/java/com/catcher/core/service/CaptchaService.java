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

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final int WIDTH = 200;
    private static final int HEIGHT = 50;

    private final UserRepository userRepository;

    private final KeyValueDataStorePort keyValueDataStorePort;

    public Captcha generateCaptchaAndSaveAnswer(final String email) {

        final var user = userRepository.findByEmail(email).orElseThrow(() -> new BaseException(BaseResponseStatus.USERS_NOT_EXISTS));

        Captcha captcha = new Captcha.Builder(WIDTH, HEIGHT)
                .addText()
                .addNoise()
                .addBackground()
                .build();

        final String generatedUserKey = generateCaptchaUserKey(user.getId());
        keyValueDataStorePort.saveValidationCodeWithUserId(generatedUserKey, captcha.getAnswer());

        return captcha;
    }

    public BufferedImage getImage(Captcha captcha) {
        return captcha.getImage();
    }

    public boolean validateCaptcha(String userEmail, String userAnswer) {
        final var user = userRepository.findByEmail(userEmail).orElseThrow(() -> new BaseException(BaseResponseStatus.USERS_NOT_EXISTS));

        final String generatedUserKEy = generateCaptchaUserKey(user.getId());
        final String answer = keyValueDataStorePort.retrieveValidationCodeWithKey(generatedUserKEy);

        return Objects.equals(answer, userAnswer);
    }

    private String generateCaptchaUserKey(final Long userId) {
        return String.format("%s_%s", userId, "CAPTCHA");
    }

}
