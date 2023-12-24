package com.catcher.core.domain.entity;

import com.catcher.core.domain.entity.enums.UserGender;
import com.catcher.core.domain.entity.enums.UserProvider;
import com.catcher.core.domain.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Where(clause = "deleted_at is null")
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

    private Date birthDate;

    @Enumerated(value = EnumType.STRING)
    private UserGender userGender;

    @Enumerated(value = EnumType.STRING)
    private UserProvider userProvider;

    @Enumerated(value = EnumType.STRING)
    private UserRole userRole;

    private LocalDateTime phoneAuthentication;

    @Column(nullable = false)
    private LocalDateTime userAgeTerm; // 필수 약관

    @Column(nullable = false)
    private LocalDateTime userServiceTerm; // 필수 약관

    @Column(nullable = false)
    private LocalDateTime userPrivacyTerm; // 필수 약관

    private LocalDateTime emailMarketingTerm; // 이메일 선택약관

    private LocalDateTime phoneMarketingTerm; // 핸드폰 선택약관

    private LocalDateTime deletedAt;

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void signOut() {
        this.deletedAt = LocalDateTime.now();
    }

    public void changeEmailTerm(boolean shouldOn) {
        emailMarketingTerm = shouldOn ? LocalDateTime.now() : null;
    }

    public void changePhoneTerm(boolean shouldOn) {
        phoneMarketingTerm = shouldOn ? LocalDateTime.now() : null;
    }

    public void changeMyInfo(String nickname, UserGender gender, Date birthDate) {
        if (!StringUtils.equals(nickname, this.nickname)) {
            this.nickname = nickname;
        }
        this.userGender = gender;
        this.birthDate = birthDate;
    }

    public void changeProfileUrl(String filename) {
        this.profileImageUrl = filename;
    }

    public void changeIntroduceContent(String content) {
        this.introduceContent = content;
    }
}
