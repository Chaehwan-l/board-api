package lch.service;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import lch.entity.Attachment;
import lch.entity.Post;
import lch.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
@RequiredArgsConstructor
public class AttachmentService {
    private final S3Client s3;
    private final AttachmentRepository repo;

    @Value("${app.aws.s3.bucket}")
    private String bucket;

    public void finalizeDraftToPost(String draftId, Post post) {
        Assert.hasText(bucket, "app.aws.s3.bucket is empty");
        Assert.hasText(draftId, "draftId is empty");
        Objects.requireNonNull(post, "post is null");
        Objects.requireNonNull(post.getId(), "post.id is null");

        String prefix = "drafts/%s/".formatted(draftId);
        var list = s3.listObjectsV2(
                ListObjectsV2Request.builder()
                        .bucket(bucket)         // 필수
                        .prefix(prefix)
                        .build());

        for (S3Object o : list.contents()) {
            String srcKey = o.key();
            String fileName = srcKey.substring(srcKey.lastIndexOf('/') + 1);
            String dstKey = "posts/%d/%s".formatted(post.getId(), fileName);

            s3.copyObject(b -> b
                    .sourceBucket(bucket)     // 필수
                    .sourceKey(srcKey)
                    .destinationBucket(bucket)// 필수
                    .destinationKey(dstKey));

            s3.deleteObject(b -> b.bucket(bucket).key(srcKey));

            Attachment a = new Attachment();
            a.setPost(post);
            a.setS3Key(dstKey);
            a.setOriginalName(fileName.substring(fileName.indexOf('_') + 1)); // 네이밍 규칙에 맞게
            a.setSize(o.size());
            repo.save(a);
        }
    }
}
