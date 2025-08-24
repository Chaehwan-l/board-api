package lch.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import lch.config.SecurityConfig;
import lch.entity.Post;
import lch.entity.UserAccount;
import lch.repository.AttachmentRepository;
import lch.repository.UserAccountRepository;
import lch.security.OAuth2SuccessHandler;
import lch.service.AttachmentService;
import lch.service.CustomOAuth2UserService;
import lch.service.PostService;

@WebMvcTest(PostViewController.class)
@Import(SecurityConfig.class)   // 보안설정 주입
@ActiveProfiles("test")
class PostViewControllerWebTest {
  @Autowired MockMvc mvc;

  @MockBean PostService postService;
  @MockBean AttachmentService attachmentService;
  @MockBean AttachmentRepository attachmentRepository;
  @MockBean UserAccountRepository userAccountRepository;
  @MockBean UserDetailsService userDetailsService;
  @MockBean CustomOAuth2UserService customOAuth2UserService;
  @MockBean OAuth2SuccessHandler oAuth2SuccessHandler;


  @Test
  void 목록_200() throws Exception {
    Page<Post> page = new PageImpl<>(List.of(new Post()));
    given(postService.search(anyString(), any(), any(Pageable.class))).willReturn(page);

    mvc.perform(get("/posts"))
       .andExpect(status().isOk())
       .andExpect(view().name("posts/list"))
       .andExpect(model().attributeExists("page","posts"));
  }

  @Test
  void 상세_200() throws Exception {
    var p = new Post(); p.setId(1L); p.setTitle("t"); p.setContent("c"); p.setUser(new UserAccount());
    given(postService.findById(1L)).willReturn(p);
    given(attachmentRepository.findByPostIdOrderByIdAsc(1L)).willReturn(List.of());

    mvc.perform(get("/posts/1"))
       .andExpect(status().isOk())
       .andExpect(view().name("posts/detail"))
       .andExpect(model().attributeExists("post","attachments","canEdit"));
  }

  @Test @WithMockUser
  void 작성폼_200() throws Exception {
    mvc.perform(get("/posts/new"))
       .andExpect(status().isOk())
       .andExpect(view().name("posts/form"))
       .andExpect(model().attributeExists("post"));
  }

  @Test @WithMockUser(username = "u1")
  void 작성_성공_리다이렉트() throws Exception {
    var me = UserAccount.builder().id(10L).username("u1").build();
    given(userAccountRepository.findByUsername("u1")).willReturn(java.util.Optional.of(me));
    var saved = new Post(); saved.setId(99L); saved.setUser(me);
    given(postService.createBy(any(UserAccount.class), anyString(), anyString())).willReturn(saved);

    mvc.perform(post("/posts").with(csrf())
             .param("title","T").param("content","C"))
       .andExpect(status().is3xxRedirection())
       .andExpect(redirectedUrl("/posts/99"));
  }

  @Test @WithMockUser(username = "u1")
  void 수정폼_200() throws Exception {
    var me = UserAccount.builder().id(10L).username("u1").build();
    given(userAccountRepository.findByUsername("u1")).willReturn(java.util.Optional.of(me));
    var post = new Post(); post.setId(1L); post.setUser(me);
    given(postService.findById(1L)).willReturn(post);
    given(attachmentRepository.findByPostIdOrderByIdAsc(1L)).willReturn(List.of());

    mvc.perform(get("/posts/1/edit"))
       .andExpect(status().isOk())
       .andExpect(view().name("posts/form"))
       .andExpect(model().attributeExists("post","attachments"));
  }

  @Test @WithMockUser(username = "u1")
  void 수정_PUT_성공_리다이렉트() throws Exception {
    var me = UserAccount.builder().id(10L).username("u1").build();
    given(userAccountRepository.findByUsername("u1")).willReturn(java.util.Optional.of(me));
    var updated = new Post(); updated.setId(1L); updated.setUser(me);
    willDoNothing().given(attachmentService).finalizeDraftToPost(anyString(), any(Post.class));
    given(postService.findById(1L)).willReturn(updated);

    mvc.perform(put("/posts/1").with(csrf())
             .param("title","T").param("content","C").param("draftId","d1"))
       .andExpect(status().is3xxRedirection())
       .andExpect(redirectedUrl("/posts/1"));
  }

  @Test @WithMockUser(username = "u1")
  void 삭제_POST_delete_리다이렉트() throws Exception {
    var me = UserAccount.builder().id(10L).username("u1").build();
    given(userAccountRepository.findByUsername("u1")).willReturn(java.util.Optional.of(me));

    mvc.perform(post("/posts/1/delete").with(csrf()))
       .andExpect(status().is3xxRedirection())
       .andExpect(redirectedUrl("/posts"));
  }

  @Test
  void 작성_비인증_로그인리다이렉트() throws Exception {
    mvc.perform(post("/posts").with(csrf()).param("title","T").param("content","C"))
       .andExpect(status().is3xxRedirection())
       .andExpect(redirectedUrl("/register?required"));
  }
}
