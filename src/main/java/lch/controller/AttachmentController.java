// lch/controller/AttachmentController.java
package lch.controller;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import lch.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Controller
@RequestMapping("/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentRepository repo;
    private final S3Presigner presigner;

    @Value("${app.aws.s3.bucket}") String bucket;

    // 파일 다운로드 (브라우저 저장창)
    @GetMapping("/{id}/download")
    public ResponseEntity<Void> download(@PathVariable("id") Long id) {
        var a = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var get = GetObjectRequest.builder()
                .bucket(bucket).key(a.getS3Key())
                .responseContentDisposition(
                    "attachment; filename=\"" +
                    UriUtils.encode(a.getOriginalName(), StandardCharsets.UTF_8) + "\"")
                .build();
        var presigned = presigner.presignGetObject(b -> b
                .getObjectRequest(get)
                .signatureDuration(Duration.ofMinutes(10)));
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(presigned.url().toString()))
                .build();
    }

    // 이미지 등 미리보기(탭에서 열기)
    @GetMapping("/{id}/inline")
    public ResponseEntity<Void> inline(@PathVariable("id") Long id) {
        var a = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var get = GetObjectRequest.builder()
                .bucket(bucket).key(a.getS3Key())
                .responseContentDisposition("inline")
                .build();
        var presigned = presigner.presignGetObject(b -> b
                .getObjectRequest(get)
                .signatureDuration(Duration.ofMinutes(10)));
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(presigned.url().toString()))
                .build();
    }
}
