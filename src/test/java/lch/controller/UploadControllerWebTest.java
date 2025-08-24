package lch.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UploadController.class)
@ActiveProfiles("test")
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class UploadControllerWebTest {
  @Autowired MockMvc mvc;

  @MockBean lch.service.FileUploadService fileUploadService;
  @MockBean lch.repository.UserAccountRepository userAccountRepository;

  @Test @WithMockUser(username = "u1")
  void 드래프트_업로드_200() throws Exception {
    var file = new MockMultipartFile("file","f.txt","text/plain","hi".getBytes());
    org.mockito.BDDMockito.given(fileUploadService.uploadDraft(org.mockito.ArgumentMatchers.anyString(),
                                                               org.mockito.ArgumentMatchers.any(org.springframework.web.multipart.MultipartFile.class)))
                          .willReturn(new lch.service.FileUploadService.UploadResult("drafts/d1/k","http://example",2L,"text/plain","f.txt"));

    mvc.perform(multipart("/api/uploads/draft").file(file).param("draftId","d1").with(csrf()))
       .andExpect(status().isOk());
  }

  @Test @WithMockUser(username = "u1")
  void 드래프트_삭제_204() throws Exception {
    org.mockito.BDDMockito.willDoNothing().given(fileUploadService)
        .deleteDraft(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());

    mvc.perform(delete("/api/uploads/draft").param("draftId","d1").param("key","drafts/d1/k").with(csrf()))
       .andExpect(status().isNoContent());
  }
}
