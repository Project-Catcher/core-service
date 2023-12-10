package com.catcher.core.domain.entity.enums;

import lombok.Getter;

/**
 * 유저 권한
 */
@Getter
public enum UserRole {
    // (1 << 0) : User Role, (1 << 1) Admin Role
    USER("ROLE_USER", 0b0001), ADMIN("ROLE_ADMIN", 0b0011);

    private final String value;
    private final int bitMask;

    UserRole(String value, int bitMask) {
        this.value = value;
        this.bitMask = bitMask;
    }
}
