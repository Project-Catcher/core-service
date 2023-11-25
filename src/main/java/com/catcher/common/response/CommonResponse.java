package com.catcher.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommonResponse<T> {
    private int code;
    private boolean success;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    @Builder
    public CommonResponse(int code, boolean success, T result) {
        this.code = code;
        this.success = success;
        this.result = result;
    }

    public static <T> CommonResponse<T> success() {
        return new CommonResponse<>(200, true, null);
    }

    public static <T> CommonResponse<T> success(T result) {
        return new CommonResponse<>(200, true, result);
    }

    public static <T> CommonResponse<T> failure(int code, T result) {
        return new CommonResponse<>(code, false, result);
    }
}
