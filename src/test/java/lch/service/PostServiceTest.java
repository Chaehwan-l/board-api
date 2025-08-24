package lch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lch.entity.Post;
import lch.entity.UserAccount;
import lch.repository.PostRepository;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
  @Mock PostRepository repo;
  @InjectMocks PostService svc;

  @Test
  void createBy_미인증_403() {
    assertThatThrownBy(() -> svc.createBy(null,"t","c"))
      .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
  }

  @Test
  void updateBy_소유자_성공() {
    var me = UserAccount.builder().id(1L).build();
    var p = new Post(); p.setId(2L); p.setUser(me);
    given(repo.findById(2L)).willReturn(Optional.of(p));

    var r = svc.updateBy(me, 2L, "T","C");
    assertThat(r.getTitle()).isEqualTo("T");
    assertThat(r.getContent()).isEqualTo("C");
  }

  @Test
  void deleteBy_타인_403() {
    var me = UserAccount.builder().id(1L).build();
    var other = UserAccount.builder().id(2L).build();
    var p = new Post(); p.setId(2L); p.setUser(other);
    given(repo.findById(2L)).willReturn(Optional.of(p));

    assertThatThrownBy(() -> svc.deleteBy(me,2L))
      .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
  }
}
