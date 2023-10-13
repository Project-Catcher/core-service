package com.catcher.core.domain.entity;

public class UserEnums {
    enum UserProvider { // 회원 가입 경로
        CATCHER, NAVER, KAKAO,
    }

    enum UserRole { // 유저 권한
        USER, ADMIN
    }
}
