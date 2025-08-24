package lch.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RegistrationController.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "app.aws.s3.bucket=test-bucket")
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class RegistrationControllerWebTest {
  @Autowired MockMvc mvc;

  @MockBean lch.service.UserAccountService userAccountService;
  @MockBean org.springframework.security.authentication.AuthenticationManager authenticationManager;
  @MockBean lch.repository.UserAccountRepository userAccountRepository;

  @Test
  void 회원가입_POST_302() throws Exception {
    var ua = lch.entity.UserAccount.builder().id(1L).username("u").email("u@e.com").role("USER").signupCompleted(true).build();
    org.mockito.BDDMockito.given(userAccountService.register(org.mockito.ArgumentMatchers.anyString(),
                                                             org.mockito.ArgumentMatchers.anyString(),
                                                             org.mockito.ArgumentMatchers.anyString()))
                          .willReturn(ua);
    org.mockito.BDDMockito.given(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any()))
                          .willReturn(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("u","p", java.util.List.of()));

    mvc.perform(post("/register")
            .param("username","u").param("email","u@e.com")
            .param("password","p").param("passwordConfirm","p")
            .with(csrf()))
       .andExpect(status().isFound())
       .andExpect(redirectedUrlPattern("/login?registered"));
  }
}
