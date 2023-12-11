package com.catcher.common;

import lombok.Getter;

@Getter
public enum BaseResponseStatus {

    SUCCESS(200, "요청에 성공하였습니다."),

    /**
     * 2000 : Request 오류
     */
    // Common
    REQUEST_ERROR(2000, "입력값을 확인해주세요."),
    EMPTY_ACCESS_JWT(2001, "Access 토큰을 입력해주세요."),
    EMPTY_REFRESH_JWT(2002, "Refresh 토큰을 입력해주세요."),
    INVALID_JWT(2003, "지원되지 않거나 잘못된 토큰 입니다."),
    NOT_EXIST_REFRESH_JWT(2005, "존재하지 않거나 만료된 Refresh 토큰입니다. 다시 로그인해주세요."),
    EXPIRED_JWT(2006, "만료된 Access 토큰입니다. Refresh 토큰을 이용해서 새로운 Access 토큰을 발급 받으세요."),
    NO_ACCESS_AUTHORIZATION(2007, "접근 권한이 없습니다."),

    // users
    USERS_DUPLICATED_USER_NAME(2100, "이미 존재하는 아이디 입니다."),
    USERS_EMPTY_USER_NAME(2101, "유저 아이디 값을 확인해주세요."),
    INVALID_USER_INFO(2102, "유저 아이디나 비밀번호를 확인해주세요."),
    INVALID_USER_CRAWLING(2103, "유저 정보를 불러오는데 실패했습니다"),
    POST_USERS_EMPTY_USER_NAME(2104, "유저 아이디를 입력해주세요."),
    INVALID_USER_NAME(2105, "아이디를 확인해주세요"),
    INVALID_USER_PW(2106, "비밀번호를 확인해주세요."),
    INVALID_USER_ID(2107, "유저 ID를 확인해주세요."),
    USERS_DUPLICATED_USER_EMAIL(2108, "이미 존재하는 이메일 입니다."),
    INVALID_USER_OAUTH_TYPE(2109, "지원하지 않는 소셜로그인 입니다."),
    USERS_DUPLICATED_USER(2110, "이미 가입한 이력이 있습니다."),
    USERS_NOT_EXISTS(2111, "존재하지 않는 유저입니다."),
    USERS_DUPLICATED_NICKNAME(2112, "이미 사용중인 닉네임입니다."),
    USERS_DUPLICATED_PHONE(2113, "이미 사용중인 핸드폰입니다."),
    AUTH_CODE_NOT_FOUND(2114, "해당 유저의 인증번호가 저장되지 않았습니다."),
    CODE_NOT_MATCH(2115, "인증번호가 일치하지 않습니다."),
    PASSWORD_NOT_MATCH(2116, "패스워드가 일치하지 않습니다."),
    EXPIRED_CODE(2117, "코드가 만료되었습니다."),
    USERS_NOT_LOGIN(2118, "로그인 정보를 찾을 수 없습니다."),

    /**
     * 3000 : Response 오류
     */
    // Common
    RESPONSE_ERROR(3000, "요청을 처리하는데 실패했습니다."),
    OAUTH_GENERATE_TOKEN_ERROR(3001, "OAuth Access 토큰 발급에 실패했습니다."),
    EMAIL_SEND_ERROR(3002, "이메일 발송에 실패했습니다."),

    /**
     * 4000 : Database, Server 오류
     */
    DATABASE_ERROR(4000, "데이터베이스 연결에 실패하였습니다."),
    REDIS_ERROR(4002, "redis 연결에 실패하였습니다."),

    /**
     * 5000: AWS Error
     */
    S3UPLOAD_ERROR(5000, "파일 업로드를 실패하였습니다."),
    KMS_ERROR(5001, "암호화 및 복호화 과정에서 실패하였습니다."),
    AWS_IO_ERROR(5002, "파일의 정보를 가져오는 데 실패했습니다."),

    ;

    private final int code;
    private final String message;

    BaseResponseStatus(int code, String message) { //BaseResponseStatus 에서 각 해당하는 코드를 생성자로 맵핑
        this.code = code;
        this.message = message;
    }
}
