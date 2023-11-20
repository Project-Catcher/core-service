package com.catcher.core.domain.entity.enums;

import lombok.Getter;

/**
 * 유저 권한
 */
@Getter
public enum UserRole {
    USER("ROLE_USER"), ADMIN("ROLE_ADMIN");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }
}
