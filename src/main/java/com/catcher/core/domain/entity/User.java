package com.catcher.core.domain.entity;

import com.catcher.core.domain.entity.enums.UserProvider;
import com.catcher.core.domain.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String phone;

    @Column(unique = true, nullable = false)
    private String email;

    private String profileImageUrl;

    private String introduceContent;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Enumerated(value = EnumType.STRING)
    private UserProvider userProvider;

    @Enumerated(value = EnumType.STRING)
    private UserRole userRole;

    private ZonedDateTime phoneAuthentication;

    @Column(nullable = false)
    private ZonedDateTime userAgeTerm; // 필수 약관

    @Column(nullable = false)
    private ZonedDateTime userServiceTerm; // 필수 약관

    @Column(nullable = false)
    private ZonedDateTime userPrivacyTerm; // 필수 약관

    private ZonedDateTime emailMarketingTerm; // 이메일 선택약관

    private ZonedDateTime phoneMarketingTerm; // 핸드폰 선택약관

    private ZonedDateTime deletedAt;
}
