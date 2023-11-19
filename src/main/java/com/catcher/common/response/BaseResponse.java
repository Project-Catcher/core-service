package com.catcher.common.response;

import com.catcher.common.BaseResponseStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.catcher.common.BaseResponseStatus.SUCCESS;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaseResponse<T> {
    @JsonProperty("isSuccess")
    private Boolean isSuccess;
    private int code;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    public BaseResponse(T result) {
        this.isSuccess = SUCCESS.isSuccess();
        this.code = SUCCESS.getCode();
        this.message = SUCCESS.getMessage();
        this.result = result;
    }

    public BaseResponse(BaseResponseStatus status) {
        this.isSuccess = status.isSuccess();
        this.message = status.getMessage();
        this.code = status.getCode();
    }

    public BaseResponse(String message){
        this.isSuccess = false;
        this.code = 5000;
        this.message = message;
    }

    public BaseResponse(BaseResponseStatus status, T result){
        this.isSuccess = status.isSuccess();
        this.message = status.getMessage();
        this.code = status.getCode();
        this.result = result;
    }
}
