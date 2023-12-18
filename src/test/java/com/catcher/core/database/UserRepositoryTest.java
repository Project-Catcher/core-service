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

import java.time.LocalDateTime;
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

    @DisplayName("존재하지 않는 이메일로 조회 조회 시 빈값 반환")
    @Test
    void invalid_search_by_email() {
        //given
        User user = registerStubUser();

        //when
        Optional<User> userOptionalA = userRepository.findByEmail(createRandomUUID());
        Optional<User> userOptionalB = userRepository.findByEmail(user.getEmail() + " ");
        Optional<User> userOptionalC = userRepository.findByEmail(" " + user.getEmail());


        //then
        assertThat(userOptionalA).isEmpty();
        assertThat(userOptionalB).isEmpty();
        assertThat(userOptionalC).isEmpty();
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

    @DisplayName("존재하지 않는 닉네임으로 조회 시 빈값 반환")
    @Test
    void invalid_search_by_nickname() {
        //given
        User user = registerStubUser();

        //when
        Optional<User> userOptionalA = userRepository.findByNickname(createRandomUUID());
        Optional<User> userOptionalB = userRepository.findByNickname(user.getNickname() + " ");
        Optional<User> userOptionalC = userRepository.findByNickname(" " + user.getNickname());

        //then
        assertThat(userOptionalA).isEmpty();
        assertThat(userOptionalB).isEmpty();
        assertThat(userOptionalC).isEmpty();
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

    @DisplayName("존재하지 않는 유저 PK로 조회 시 빈값 반환")
    @Test
    void invalid_search_by_id() {
        //given
        User user = registerStubUser();

        //when
        Optional<User> userOptionalA = userRepository.findById(user.getId() + 1L);
        Optional<User> userOptionalB = userRepository.findById(user.getId() - 1L);

        //then
        assertThat(userOptionalA).isEmpty();
        assertThat(userOptionalB).isEmpty();
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

    @DisplayName("존재하지 않는 핸드폰으로 조회 시 빈값 반환")
    @Test
    void invalid_search_by_phone() {
        //given
        User user = registerStubUser();

        //when
        Optional<User> userOptionalA = userRepository.findByPhone(createRandomUUID());
        Optional<User> userOptionalB = userRepository.findByPhone(user.getPhone() +" ");
        Optional<User> userOptionalC = userRepository.findByPhone(" " + user.getPhone());

        //then
        assertThat(userOptionalA).isEmpty();
        assertThat(userOptionalB).isEmpty();
        assertThat(userOptionalC).isEmpty();
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

    @DisplayName("존재하지 않는 유저아이디로 조회시 예외발생")
    @Test
    void invalid_search_by_username() {
        //given
        User user = registerStubUser();

        //when
        Optional<User> userOptionalA = userRepository.findByUsername(createRandomUUID());
        Optional<User> userOptionalB = userRepository.findByUsername(user.getUsername() + " ");
        Optional<User> userOptionalC = userRepository.findByUsername(" " + user.getUsername());

        //then
        assertThat(userOptionalA).isEmpty();
        assertThat(userOptionalB).isEmpty();
        assertThat(userOptionalC).isEmpty();
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