package com.catcher.core.domain.entity;

import com.catcher.core.domain.entity.enums.UserProvider;
import com.catcher.core.domain.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uid;

    @Column(unique = true, nullable = false)
    private String username;

    // TODO: encrypted maybe?
    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String phone;

    @Column(unique = true, nullable = false)
    private String email;

    // TODO : 첨부파일 외래키 참조해야 지
    private String profileImageUrl;

    private String introduceContent;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Enumerated(value = EnumType.STRING)
    private UserProvider userProvider;

    @Enumerated(value = EnumType.STRING)
    private UserRole userRole;

    @Column(nullable = false) // 필수 약관이라 가정
    private LocalDateTime userAgeTerm;

    private LocalDateTime userServiceTerm; // 선택 약관이라 가정

    private LocalDateTime userPrivacyTerm;

    private LocalDateTime userLocationTerm;

    private LocalDateTime userMarketingTerm;

    private LocalDateTime deletedAt;

    @Builder
    public User(String uid, String password, String username, String email, String profileImageUrl, String phone, String nickname, UserRole role, LocalDateTime userAgeTerm, LocalDateTime userServiceTerm, LocalDateTime userPrivacyTerm, LocalDateTime userLocationTerm, String introduceContent, LocalDateTime userMarketingTerm){
        this.uid = uid;
        this.password = password;
        this.username = username;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.phone = phone;
        this.nickname = nickname;
        this.userRole = role;
        this.userAgeTerm = userAgeTerm;
        this.userServiceTerm = userServiceTerm;
        this.userPrivacyTerm = userPrivacyTerm;
        this.userLocationTerm = userLocationTerm;
        this.introduceContent = introduceContent;
        this.userMarketingTerm = userMarketingTerm;
    }
}
