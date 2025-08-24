package lch.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import lch.entity.Post;
import lch.entity.UserAccount;

@DataJpaTest
@ActiveProfiles("test")
class PostRepositoryTest {

    @Autowired PostRepository postRepository;
    @Autowired UserAccountRepository userRepository;

    @Test
    @DisplayName("제목/작성자 부분검색 + 페이징")
    void search_with_paging() {
        var u1 = UserAccount.builder().username("alpha").email("a@a.com").password("x").signupCompleted(true).build();
        var u2 = UserAccount.builder().username("beta").email("b@b.com").password("x").signupCompleted(true).build();
        userRepository.save(u1); userRepository.save(u2);

        var p1 = new Post(); p1.setTitle("Hello World"); p1.setContent("A"); p1.setUser(u1);
        var p2 = new Post(); p2.setTitle("Another note"); p2.setContent("B"); p2.setUser(u2);
        var p3 = new Post(); p3.setTitle("HELIX"); p3.setContent("C"); p3.setUser(u1);
        postRepository.save(p1); postRepository.save(p2); postRepository.save(p3);

        Page<Post> byTitle = postRepository.findByTitleContainingIgnoreCase("he", PageRequest.of(0, 10));
        assertThat(byTitle.getTotalElements()).isEqualTo(3L);

        Page<Post> byAuthor = postRepository.findByUser_UsernameContainingIgnoreCase("a", PageRequest.of(0, 10));
        assertThat(byAuthor.getTotalElements()).isEqualTo(3L);
    }
}
