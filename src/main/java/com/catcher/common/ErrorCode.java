package com.catcher.common;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(400, " Invalid input value"),
    METHOD_NOT_ALLOWED(405,  " Method not allowed"),
    ENTITY_NOT_FOUND(400,  " Entity not found"),
    INVALID_TYPE_VALUE(400, " Invalid type value"),
    NOT_AUTHENTICATED(403, " Not authenticated"),
    EXTERNAL_ERROR(500, " External exception"),
    INTERNAL_SERVER_ERROR(500, " Server error"),
    ;

    private final String message;
    private final int status;

    ErrorCode(final int status, final String message) {
        this.status = status;
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
    public int getStatus() {
        return status;
    }
}
