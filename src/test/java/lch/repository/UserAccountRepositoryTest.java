package lch.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import lch.entity.UserAccount;

@DataJpaTest
@ActiveProfiles("test")
class UserAccountRepositoryTest {
    @Autowired UserAccountRepository userRepository;

    @Test
    @DisplayName("UserAccount 저장/조회")
    void save_and_find() {
        var u = UserAccount.builder()
                .username("u1").email("u1@example.com")
                .password("x").signupCompleted(true).build();

        var saved = userRepository.save(u);

        assertThat(saved.getId()).isNotNull();
        Optional<UserAccount> byName = userRepository.findByUsername("u1");
        assertThat(byName).isPresent();
        assertThat(userRepository.findByEmail("u1@example.com")).isPresent();
    }
}
