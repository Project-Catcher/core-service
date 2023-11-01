package com.catcher.core.dto.oauth;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OAuthCreateRequest {
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

    @NotNull(message = "액세스 토큰이 없습니다.")
    private String accessToken;

    private int role;
    private String introduceContent;
    private LocalDateTime marketingTerm;

}
