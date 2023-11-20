package com.catcher.common;

import com.catcher.common.exception.BaseException;
import com.catcher.common.response.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.catcher.common.BaseResponseStatus.REQUEST_ERROR;
import static com.catcher.common.BaseResponseStatus.RESPONSE_ERROR;

@Slf4j
@RestControllerAdvice({"com.catcher.infrastructure", "com.catcher.resource"})
public class CatcherControllerAdvice {

    @ExceptionHandler(BaseException.class)
    public CommonResponse handle(BaseException e) {
        BaseResponseStatus status = e.getStatus();
        return CommonResponse.failure(status.getCode(), status.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResponse handle(MethodArgumentNotValidException e) {
        String defaultMessage = e.getAllErrors().get(0).getDefaultMessage();
        log.error(defaultMessage);
        return CommonResponse.failure(REQUEST_ERROR.getCode(), e.getAllErrors().get(0).getDefaultMessage());
    }

    @ExceptionHandler(Exception.class)
    public CommonResponse handle(Exception e) {
        log.error("", e);
        return CommonResponse.failure(RESPONSE_ERROR.getCode(), RESPONSE_ERROR.getMessage());
    }
}
