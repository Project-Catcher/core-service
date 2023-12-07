package com.catcher.testconfiguriation;

import com.catcher.common.BaseResponseStatus;
import com.catcher.common.exception.BaseException;
import org.assertj.core.api.Assertions;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class BaseExceptionUtils extends Assertions {
    private BaseExceptionUtils() {
    }

    public static void assertBaseException(Runnable runnable, BaseResponseStatus instance) {
        Assertions.assertThat(
                assertThrows(BaseException.class, () -> {
                    runnable.run();
                }).getStatus()
        ).isEqualTo(instance);
    }
}
