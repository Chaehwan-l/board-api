package lch.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import lch.entity.Attachment;
import lch.entity.Post;
import lch.entity.UserAccount;
import lch.repository.AttachmentRepository;
import lch.repository.UserAccountRepository;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@WebMvcTest(AttachmentController.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "app.aws.s3.bucket=test-bucket")
@WithMockUser(username = "u1", roles = "USER")
class AttachmentControllerWebTest {
  @Autowired MockMvc mvc;

  @MockBean AttachmentRepository repo;
  @MockBean S3Presigner presigner;
  @MockBean S3Client s3;
  @MockBean UserAccountRepository userRepo;

  @Test
  void 다운로드_302() throws Exception {
    var a = new Attachment(); a.setId(1L); a.setS3Key("posts/1/a.txt"); a.setOriginalName("a.txt");
    given(repo.findById(1L)).willReturn(Optional.of(a));

    PresignedGetObjectRequest pre = mock(PresignedGetObjectRequest.class);
    given(pre.url()).willReturn(new URL("https://example.com/a.txt"));
    given(presigner.presignGetObject(any(Consumer.class))).willReturn(pre);

    mvc.perform(get("/attachments/1/download"))
       .andExpect(status().isFound())
       .andExpect(header().string("Location", "https://example.com/a.txt"));
  }

  @Test
  void 인라인_302() throws Exception {
    var a = new Attachment(); a.setId(1L); a.setS3Key("posts/1/a.txt"); a.setOriginalName("a.txt");
    given(repo.findById(1L)).willReturn(Optional.of(a));

    PresignedGetObjectRequest pre = mock(PresignedGetObjectRequest.class);
    given(pre.url()).willReturn(new URL("https://example.com/a.txt"));
    given(presigner.presignGetObject(any(Consumer.class))).willReturn(pre);

    mvc.perform(get("/attachments/1/inline"))
       .andExpect(status().isFound())
       .andExpect(header().string("Location", "https://example.com/a.txt"));
  }

  @Test
  void 삭제_204() throws Exception {
    var me = UserAccount.builder().id(7L).username("u1").build();
    given(userRepo.findByUsername("u1")).willReturn(Optional.of(me));

    var post = new Post(); post.setUser(me);
    var a = new Attachment(); a.setId(1L); a.setPost(post); a.setS3Key("k");
    given(repo.findById(1L)).willReturn(Optional.of(a));

    mvc.perform(delete("/attachments/1").with(csrf()))
       .andExpect(status().isNoContent());

    then(s3).should().deleteObject(any(Consumer.class));
    then(repo).should().delete(a);
  }
}