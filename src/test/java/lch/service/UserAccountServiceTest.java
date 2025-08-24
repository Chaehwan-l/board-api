package lch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import lch.entity.UserAccount;
import lch.repository.UserAccountRepository;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

  @Mock UserAccountRepository repo;
  @Mock PasswordEncoder encoder;
  @InjectMocks UserAccountService svc;

  @Test
  void register_중복유저_예외() {
    given(repo.findByUsername("u")).willReturn(Optional.of(new UserAccount()));
    assertThatThrownBy(() -> svc.register("u","u@e.com","p"))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void register_성공_비번인코딩() {
    given(repo.findByUsername("u")).willReturn(Optional.empty());
    given(repo.findByEmail("u@e.com")).willReturn(Optional.empty());
    given(encoder.encode("p")).willReturn("EP");
    var saved = new UserAccount(); saved.setId(1L); saved.setUsername("u"); saved.setEmail("u@e.com");
    given(repo.save(any(UserAccount.class))).willReturn(saved);

    var r = svc.register("u","u@e.com","p");

    assertThat(r.getId()).isEqualTo(1L);
    then(encoder).should().encode("p");
  }
}
