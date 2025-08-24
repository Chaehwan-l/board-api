package lch.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import lch.entity.Attachment;
import lch.entity.Post;
import lch.entity.UserAccount;

@DataJpaTest
@ActiveProfiles("test")
class AttachmentRepositoryTest {
    @Autowired AttachmentRepository attachmentRepository;
    @Autowired PostRepository postRepository;
    @Autowired UserAccountRepository userRepository;

    @Test
    @DisplayName("Attachment 저장/조회")
    void save_and_find() {
        var u = UserAccount.builder()
                .username("u1").email("u1@example.com")
                .password("x").signupCompleted(true).build();
        userRepository.save(u);

        var p = new Post();
        p.setTitle("t");
        p.setContent("c");
        p.setUser(u);
        postRepository.save(p);

        var a = new Attachment();
        a.setPost(p);
        a.setSize(123L);
        a.setContentType("text/plain");
        a.setS3Key("k1");
        a.setOriginalName("n1.txt");

        var saved = attachmentRepository.save(a);

        assertThat(saved.getId()).isNotNull();
        assertThat(attachmentRepository.findByPostIdOrderByIdAsc(p.getId())).hasSize(1);
        assertThat(saved.getPost().getId()).isEqualTo(p.getId());
    }
}
