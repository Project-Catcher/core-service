package com.catcher.core.domain.entity;

import com.catcher.core.domain.entity.enums.UserProvider;
import com.catcher.core.domain.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
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
    private String userId;

    // TODO: encrypted maybe?
    @Column(nullable = false)
    private String password;

    private String phone;

    // TODO : 첨부파일 외래키 참조해야 지
    private String profileImageUrl;

    @Enumerated(value = EnumType.STRING)
    private UserProvider userProvider;

    @Enumerated(value = EnumType.STRING)
    private UserRole userRole;

    @Column(nullable = false) // 필수 약관이라 가정
    private LocalDateTime userTerm1;

    private LocalDateTime userTerm2; // 선택 약관이라 가정

    private LocalDateTime userTerm3;

    private LocalDateTime userTerm4;

    private LocalDateTime deletedAt;

}
