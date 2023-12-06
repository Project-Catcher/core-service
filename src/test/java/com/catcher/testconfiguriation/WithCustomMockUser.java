package com.catcher.testconfiguriation;

import com.catcher.core.domain.entity.enums.UserRole;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.ZonedDateTime;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCustomMockUserSecurityContextFactory.class)
public @interface WithCustomMockUser{
    long id() default 1L;
    String username() default "username";
    String password() default "password";
    String phone() default "phone";
    String email() default "email";
    String profileImageUrl() default "profileImageUrl";
    String introduceContent() default "introduceContent";
    String nickname() default "nickname";
    UserRole role();
}
