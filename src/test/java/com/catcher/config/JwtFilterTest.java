package com.catcher.config;

import com.catcher.app.AppApplication;
import com.catcher.common.exception.BaseException;
import com.catcher.core.database.DBManager;
import com.catcher.core.database.UserRepository;
import com.catcher.core.domain.entity.User;
import com.catcher.core.domain.entity.enums.UserRole;
import com.catcher.core.service.AuthService;
import com.catcher.core.service.UserService;
import com.catcher.testconfiguriation.EmbeddedRedisConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.catcher.core.domain.entity.enums.UserProvider.CATCHER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest(classes = {AppApplication.class, EmbeddedRedisConfiguration.class})
class JwtFilterTest {

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserRepository userRepository;
    @PersistenceContext
    EntityManager em;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    DBManager dbManager;
    @Autowired
    UserService userService;
    @Autowired
    AuthService authService;

    JwtFilter jwtFilter;

    @BeforeEach
    void beforeEach() {
        jwtFilter = new JwtFilter(jwtTokenProvider, dbManager);
    }

    @DisplayName("Access 토큰 없을 시 그냥 통과")
    @Test
    void no_access_token_filter() throws ServletException, IOException {
        //given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        //when
        Mockito.when(request.getHeader("Authorization")).thenReturn(null);

        //then
        jwtFilter.doFilterInternal(request, response, filterChain);
    }

    @DisplayName("유효하지 않은 Access 토큰이면, 예외 발생")
    @Test
    void invalid_access_token_filter() {
        //given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        //when
        Mockito.when(request.getHeader("Authorization")).thenReturn("gadhdfshea.adsfsgasgh.asdasfafsf");

        //then
        assertThatThrownBy(() -> jwtFilter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(BaseException.class);
    }

    @DisplayName("유효한 Access 토큰이면, Authentication이 정상 설정")
    @Test
    @Transactional
    void valid_access_token_filter() throws ServletException, IOException {
        //given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        User user = registerStubUser();
        Authentication authentication = createAuthentication(user.getUsername(), user.getPassword());
        String accessToken = jwtTokenProvider.createAccessToken(authentication);

        //when
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer " + accessToken);
        jwtFilter.doFilterInternal(request, response,filterChain);

        //then
        Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(currentAuthentication).isNotNull();
        assertThat(currentAuthentication.getName()).isEqualTo(user.getUsername());
    }

    @DisplayName("로그아웃한 Access 토큰이면, Authentication이 설정이 되면 안된다.")
    @Test
    @Transactional
    void invalid_logout_access_token_filter() throws ServletException, IOException {
        //given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);
        User user = registerStubUser();
        Authentication authentication = createAuthentication(user.getUsername(), user.getPassword());
        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        authService.discardAccessToken("Bearer " + accessToken);
        //when
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer " + accessToken);
        jwtFilter.doFilterInternal(request, response,filterChain);
        //then
        Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(currentAuthentication).isNull();
    }

    private Authentication createAuthentication(String username, String password) {
        return new UsernamePasswordAuthenticationToken(
                username,
                password
        );
    }

    private User registerStubUser() {
        User user = User.builder()
                .username(createRandomUUID())
                .password(passwordEncoder.encode(createRandomUUID()))
                .phone(createRandomUUID())
                .email(createRandomUUID())
                .profileImageUrl(null)
                .introduceContent(null)
                .nickname(createRandomUUID())
                .userProvider(CATCHER)
                .userRole(UserRole.USER)
                .userAgeTerm(LocalDateTime.now())
                .userServiceTerm(LocalDateTime.now())
                .userPrivacyTerm(LocalDateTime.now())
                .emailMarketingTerm(LocalDateTime.now())
                .phoneMarketingTerm(LocalDateTime.now())
                .build();

        userRepository.save(user);
        flushAndClearPersistence();
        return user;
    }

    private void flushAndClearPersistence() {
        em.flush();
        em.clear();
    }

    private String createRandomUUID() {
        return UUID.randomUUID().toString();
    }
}