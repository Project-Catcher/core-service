package com.catcher.core.domain.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum UserGender {
    MALE, FEMALE;

    @JsonCreator
    public static UserGender from(String s) {
        return UserGender.valueOf(s.toUpperCase());
    }
}
