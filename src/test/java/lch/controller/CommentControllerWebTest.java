package lch.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import lch.entity.Comment;
import lch.entity.UserAccount;
import lch.repository.UserAccountRepository;
import lch.service.CommentService;

@ActiveProfiles("test")
@WebMvcTest(CommentController.class)
@TestPropertySource(properties = "app.aws.s3.bucket=test-bucket")
class CommentControllerWebTest {

  @Autowired MockMvc mvc;

  @MockBean CommentService commentService;
  @MockBean UserAccountRepository userRepo;

  @Test
  @WithMockUser(username = "u1", roles = "USER")
  void 댓글_등록_302() throws Exception {
    var me = UserAccount.builder().id(7L).username("u1").build();
    given(userRepo.findByUsername("u1")).willReturn(Optional.of(me));
    given(commentService.add(me, 3L, "안녕")).willReturn(new Comment());

    mvc.perform(post("/posts/3/comments").param("content","안녕").with(csrf()))
       .andExpect(status().isFound())
       .andExpect(redirectedUrl("/posts/3#comments"));
  }

  @Test
  @WithMockUser(username = "u1", roles = "USER")
  void 댓글_삭제_form_override_302() throws Exception {
    var me = UserAccount.builder().id(7L).username("u1").build();
    given(userRepo.findByUsername("u1")).willReturn(Optional.of(me));
    willDoNothing().given(commentService).delete(me, 11L);

    mvc.perform(post("/comments/11")
            .param("_method","delete")
            .param("postId","3")
            .with(csrf()))
       .andExpect(status().isFound())
       .andExpect(redirectedUrl("/posts/3#comments"));

    then(commentService).should().delete(me, 11L);
  }

  @Test
  @WithMockUser(username = "u1", roles = "USER")
  void 댓글_삭제_DELETE_302() throws Exception {
    var me = UserAccount.builder().id(7L).username("u1").build();
    given(userRepo.findByUsername("u1")).willReturn(Optional.of(me));
    willDoNothing().given(commentService).delete(me, 12L);

    mvc.perform(delete("/comments/12").param("postId","3").with(csrf()))
       .andExpect(status().isFound())
       .andExpect(redirectedUrl("/posts/3#comments"));
  }

  @Test
  @WithAnonymousUser
  void 인증없음_댓글등록_리다이렉트() throws Exception {
    mvc.perform(post("/posts/99/comments").param("content","x").with(csrf()))
       .andExpect(status().is3xxRedirection())
       .andExpect(redirectedUrlPattern("**/login*"));
  }
}
