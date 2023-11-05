package com.catcher.core.domain.entity;

import com.catcher.core.domain.entity.enums.UserProvider;
import com.catcher.core.domain.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(nullable = false)
    private ZonedDateTime userAgeTerm; // 필수 약관

    @Column(nullable = false)
    private ZonedDateTime userServiceTerm; // 필수 약관

    @Column(nullable = false)
    private ZonedDateTime userPrivacyTerm; // 필수 약관

    @Column(nullable = false)
    private ZonedDateTime userLocationTerm; // 필수 약관

    private ZonedDateTime userMarketingTerm; // 선택 약관

    private ZonedDateTime deletedAt;

    @Builder
    public User(String username, String password,  String email, String profileImageUrl, String phone, String nickname, UserProvider userProvider, UserRole role, ZonedDateTime userAgeTerm, ZonedDateTime userServiceTerm, ZonedDateTime userPrivacyTerm, ZonedDateTime userLocationTerm, String introduceContent, ZonedDateTime userMarketingTerm){
        this.username = username;
        this.password = password;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.userProvider = userProvider;
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
