package com.catcher.security.aop;

import com.catcher.common.BaseResponseStatus;
import com.catcher.common.exception.BaseException;
import com.catcher.core.domain.entity.enums.UserRole;
import com.catcher.security.CatcherUser;
import com.catcher.security.annotation.AuthorizationRequired;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class AuthorizationAop {

    @Before("@annotation(com.catcher.security.annotation.AuthorizationRequired)")
    public void processCustom(JoinPoint joinPoint) {
        AuthorizationRequired annotation = getAnnotation(joinPoint);

        try {
            UserRole currentUserRole = getCurrentUserRole();
            UserRole[] userRoles = annotation.value();

            if (!checkUserRoles(userRoles, currentUserRole)) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.NO_ACCESS_AUTHORIZATION);
        }
    }

    private boolean checkUserRoles(UserRole[] userRoles, UserRole role) {
        for (UserRole userRole : userRoles) {
            if ((userRole.getBitMask() & role.getBitMask()) > 0)
                return true;
        }
        return false;
    }

    private AuthorizationRequired getAnnotation(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        return method.getAnnotation(AuthorizationRequired.class);
    }

    private UserRole getCurrentUserRole() {
        CatcherUser catcherUser = (CatcherUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return catcherUser.getUser().getUserRole();
    }
}