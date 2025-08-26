package lch.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import lch.entity.Comment;
import lch.entity.Post;
import lch.entity.UserAccount;

@ActiveProfiles("test")
@DataJpaTest
class CommentRepositoryTest {

  @Autowired CommentRepository comments;
  @Autowired PostRepository posts;
  @Autowired UserAccountRepository users;

  @Test
  void Post별_정렬조회() {
    var u = UserAccount.builder().username("u1").email("u1@e.com").password("x").role("USER").build();
    users.save(u);
    var p = new Post(); p.setTitle("t"); p.setContent("c"); p.setUser(u);
    posts.save(p);

    var c1 = new Comment(); c1.setPost(p); c1.setUser(u); c1.setContent("a");
    var c2 = new Comment(); c2.setPost(p); c2.setUser(u); c2.setContent("b");
    comments.save(c2);
    comments.save(c1);

    List<Comment> list = comments.findByPost_IdOrderByIdAsc(p.getId());
    assertThat(list).hasSize(2);
    assertThat(list.get(0).getId()).isLessThan(list.get(1).getId());
  }
}
