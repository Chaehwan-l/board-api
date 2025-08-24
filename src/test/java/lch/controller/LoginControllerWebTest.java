package lch.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import lch.repository.UserAccountRepository;

@WebMvcTest(LoginController.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "app.aws.s3.bucket=test-bucket")
class LoginControllerWebTest {
  @Autowired MockMvc mvc;

  @MockBean UserAccountRepository userAccountRepository;

  @Test
  void 로그인_페이지_200() throws Exception {
    mvc.perform(get("/login"))
       .andExpect(status().isOk());
  }
}