package com.catcher.testconfiguriation;

import com.catcher.core.domain.entity.User;
import com.catcher.core.domain.entity.enums.UserRole;
import com.catcher.security.CatcherUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import java.time.LocalDateTime;

import static com.catcher.core.domain.entity.enums.UserProvider.CATCHER;


public class WithCustomMockUserSecurityContextFactory implements WithSecurityContextFactory<WithCustomMockUser> {

    @Override
    public SecurityContext createSecurityContext(WithCustomMockUser annotation) {
        Long id = annotation.id();
        String username = annotation.username();
        String password = annotation.password();
        String phone = annotation.phone();
        String email = annotation.email();
        String profileImageUrl = annotation.profileImageUrl();
        String introduceContent = annotation.introduceContent();
        String nickname = annotation.nickname();
        UserRole role = annotation.role();

        CatcherUser user =
                new CatcherUser(createUser(id, username, password, phone, email, profileImageUrl, introduceContent, nickname, role));

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(user, password);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);
        return context;
    }

    private User createUser(long id, String username, String password, String phone, String email, String profileImageUrl, String introduceContent, String nickname, UserRole role) {
        return User.builder()
                .id(id)
                .username(username)
                .password(password)
                .phone(phone)
                .email(email)
                .profileImageUrl(profileImageUrl)
                .introduceContent(introduceContent)
                .nickname(nickname)
                .userProvider(CATCHER)
                .userRole(role)
                .userAgeTerm(LocalDateTime.now())
                .userServiceTerm(LocalDateTime.now())
                .userPrivacyTerm(LocalDateTime.now())
                .emailMarketingTerm(LocalDateTime.now())
                .phoneMarketingTerm(LocalDateTime.now())
                .build();
    }
}
