package com.catcher.core.database;

import com.catcher.app.AppApplication;
import com.catcher.core.domain.entity.User;
import com.catcher.core.domain.entity.enums.UserRole;
import com.catcher.testconfiguriation.EmbeddedRedisConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.catcher.core.domain.entity.enums.UserProvider.CATCHER;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(classes = {AppApplication.class, EmbeddedRedisConfiguration.class})
@Transactional
class UserRepositoryTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PersistenceContext
    EntityManager em;

    @DisplayName("이메일로 조회")
    @Test
    void search_by_email() {
        //given
        User user = registerStubUser();

        //when
        Optional<User> userOptional = userRepository.findByEmail(user.getEmail());

        //then
        assertThat(userOptional).isPresent();
    }

    @DisplayName("닉네임으로 조회")
    @Test
    void search_by_nickname() {
        //given
        User user = registerStubUser();

        //when
        Optional<User> userOptional = userRepository.findByNickname(user.getNickname());

        //then
        assertThat(userOptional).isPresent();
    }

    @DisplayName("유저 PK로 조회")
    @Test
    void search_by_id() {
        //given
        User user = registerStubUser();

        //when
        Optional<User> userOptional = userRepository.findById(user.getId());

        //then
        assertThat(userOptional).isPresent();
    }

    @DisplayName("핸드폰으로 조회")
    @Test
    void search_by_phone() {
        //given
        User user = registerStubUser();

        //when
        Optional<User> userOptional = userRepository.findByPhone(user.getPhone());

        //then
        assertThat(userOptional).isPresent();
    }

    @DisplayName("유저아이디로 조회")
    @Test
    void search_by_username() {
        //given
        User user = registerStubUser();

        //when
        Optional<User> userOptional = userRepository.findByUsername(user.getUsername());

        //then
        assertThat(userOptional).isPresent();
    }

    private User registerStubUser() {
        User user = User.builder()
                .username(createRandomUUID())
                .password(createRandomUUID())
                .phone(createRandomUUID())
                .email(createRandomUUID())
                .profileImageUrl(null)
                .introduceContent(null)
                .nickname(createRandomUUID())
                .userProvider(CATCHER)
                .role(UserRole.USER)
                .userAgeTerm(ZonedDateTime.now())
                .userServiceTerm(ZonedDateTime.now())
                .userPrivacyTerm(ZonedDateTime.now())
                .userLocationTerm(ZonedDateTime.now())
                .userMarketingTerm(ZonedDateTime.now())
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