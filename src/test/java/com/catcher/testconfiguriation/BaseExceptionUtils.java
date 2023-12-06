package com.catcher.testconfiguriation;

import com.catcher.common.exception.BaseException;
import org.assertj.core.api.Assertions;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class BaseExceptionUtils extends Assertions {
    private BaseExceptionUtils() {
    }

    public static BaseException assertException(Runnable runnable) {
        return assertThrows(BaseException.class, () -> {
            runnable.run();
        });
    }
}
