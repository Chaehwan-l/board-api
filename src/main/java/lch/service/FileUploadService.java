package lch.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final S3Client s3;
    private final S3Presigner presigner;

    @Value("${app.aws.s3.bucket}")
    private String bucket;

    public record UploadResult(String key, String url, long size, String contentType, String originalName) {}

    public UploadResult uploadDraft(String draftId, MultipartFile file) throws IOException {
        String original = sanitize(file.getOriginalFilename());
        String key = "drafts/%s/%s_%s".formatted(draftId, UUID.randomUUID(), original);

        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket).key(key)
                .contentType(file.getContentType())
                .build();

        s3.putObject(put, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(bucket).key(key).build();

        // 요청(Pre*sign*GetObject) → 결과(Pre*signed*GetObject)
        PresignedGetObjectRequest pre = presigner.presignGetObject(
        		GetObjectPresignRequest.builder()
                        .getObjectRequest(get)
                        .signatureDuration(Duration.ofHours(24))
                        .build());

        return new UploadResult(key, pre.url().toString(), file.getSize(),
                file.getContentType(), original);
    }

    private String sanitize(String name) {
        if (name == null) {
			return "file";
		}
        String base = name.replace("\\","/");
        base = base.substring(base.lastIndexOf('/') + 1);
        return URLEncoder.encode(base, StandardCharsets.UTF_8);
    }
}
