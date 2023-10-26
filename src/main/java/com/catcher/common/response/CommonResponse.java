package com.catcher.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CommonResponse<T> {
    private final int code;
    private final boolean success;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T result;

    @Builder
    public CommonResponse(int code, boolean success, T result) {
        this.code = code;
        this.success = success;
        this.result = result;
    }

    public static <T> CommonResponse<T> success(int code, T result) {
        return new CommonResponse<>(code, true, result);
    }
}
