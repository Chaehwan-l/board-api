package lch.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lch.service.FileUploadService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {
    private final FileUploadService fileUploadService;

    @PostMapping(
        path = "/draft",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public FileUploadService.UploadResult uploadDraft(
            @RequestParam("draftId") String draftId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return fileUploadService.uploadDraft(draftId, file);
    }

    @DeleteMapping(path = "/draft")
    public ResponseEntity<Void> deleteDraft(
            @RequestParam("draftId") String draftId,
            @RequestParam("key") String key) {
        fileUploadService.deleteDraft(draftId, key);
        return ResponseEntity.noContent().build();
    }
}
