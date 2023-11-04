package com.catcher.common;

import com.catcher.common.exception.BaseException;
import com.catcher.common.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.catcher.common.BaseResponseStatus.*;

@Slf4j
@RestControllerAdvice
public class CatcherControllerAdvice {

    @ExceptionHandler(BaseException.class)
    public BaseResponse handle(BaseException e) {
        return new BaseResponse(e.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse handle(MethodArgumentNotValidException e) {
        String defaultMessage = e.getAllErrors().get(0).getDefaultMessage();
        log.error(defaultMessage);
        return new BaseResponse(defaultMessage);
    }

    @ExceptionHandler(Exception.class)
    public BaseResponse handle(Exception e) {
        log.error("", e);
        return new BaseResponse(RESPONSE_ERROR);
    }
}
