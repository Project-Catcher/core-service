package com.catcher.core.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCreateRequest {
    @NotNull(message = "이름을 입력하세요.")
    private String name;

    @NotNull(message = "아이디를 입력하세요.")
    private String username;

    @NotNull(message = "비밀번호를 입력하세요.")
    private String password;

    @NotNull(message = "이메일 주소를 입력하세요.")
    private String email;

    @NotNull(message = "휴대폰 번호를 입력하세요.")
    private String phone;

    @NotNull(message = "닉네임을 입력하세요.")
    private String nickname;

    @NotNull(message = "필수 약관 14세 이상 동의해주세요.")
    private LocalDateTime ageTerm;

    @NotNull(message = "필수 약관 서비스 이용 동의해주세요.")
    private LocalDateTime serviceTerm;

    @NotNull(message = "필수 약관 개인정보 이용 동의해주세요.")
    private LocalDateTime privacyTerm;

    @NotNull(message = "필수 약관 위치 정보 이용 동의해주세요.")
    private LocalDateTime locationTerm;

    private int role;
    private String introduceContent;
    private LocalDateTime marketingTerm;

    @Builder
    public UserCreateRequest(String name, String username, String password, int role, String email, String phone, String nickname, LocalDateTime ageTerm, LocalDateTime serviceTerm, LocalDateTime privacyTerm, LocalDateTime locationTerm, String introduceContent, LocalDateTime marketingTerm){
        this.name = name;
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        this.phone = phone;
        this.nickname = nickname;
        this.ageTerm = ageTerm;
        this.serviceTerm = serviceTerm;
        this.privacyTerm = privacyTerm;
        this.locationTerm = locationTerm;
        this.introduceContent = introduceContent;
        this.marketingTerm = marketingTerm;
    }
}

