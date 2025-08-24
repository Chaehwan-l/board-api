package lch.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(CurrentUserAdvice.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class CurrentUserAdviceWebTest {
  @Autowired MockMvc mvc;
  @org.springframework.boot.test.mock.mockito.MockBean
  lch.repository.UserAccountRepository userAccountRepository;

  @org.junit.jupiter.api.Test
  void ok() throws Exception {
    mvc.perform(get("/")).andExpect(status().isOk());
  }
}
