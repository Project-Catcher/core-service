package com.catcher.core.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserCreateRequest {
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ageTerm;

    @NotNull(message = "필수 약관 서비스 이용 동의해주세요.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime serviceTerm;

    @NotNull(message = "필수 약관 개인정보 이용 동의해주세요.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime privacyTerm;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime marketingTerm;
}

