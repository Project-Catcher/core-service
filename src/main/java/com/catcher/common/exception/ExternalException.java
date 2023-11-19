package com.catcher.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExternalException extends CommonException{
    private int code;
    private String message;
}
