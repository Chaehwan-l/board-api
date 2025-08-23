// lch/controller/AttachmentController.java
package lch.controller;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import lch.entity.Attachment;
import lch.entity.UserAccount;
import lch.repository.AttachmentRepository;
import lch.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Controller
@RequestMapping("/attachments")
@RequiredArgsConstructor
public class AttachmentController {

	private final AttachmentRepository repo;
	private final S3Presigner presigner;
	private final S3Client s3;
	private final UserAccountRepository userRepo;

	@Value("${app.aws.s3.bucket}")
	String bucket;

	// 파일 다운로드 (브라우저 저장창)
	@GetMapping("/{id}/download")
	public ResponseEntity<Void> download(@PathVariable("id") Long id) {
		var a = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		var get = GetObjectRequest.builder().bucket(bucket).key(a.getS3Key())
				.responseContentDisposition(
						"attachment; filename=\"" + UriUtils.encode(a.getOriginalName(), StandardCharsets.UTF_8) + "\"")
				.build();
		var presigned = presigner
				.presignGetObject(b -> b.getObjectRequest(get).signatureDuration(Duration.ofMinutes(10)));
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(presigned.url().toString())).build();
	}

	// 이미지 등 미리보기(탭에서 열기)
	@GetMapping("/{id}/inline")
	public ResponseEntity<Void> inline(@PathVariable("id") Long id) {
		var a = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		var get = GetObjectRequest.builder().bucket(bucket).key(a.getS3Key()).responseContentDisposition("inline")
				.build();
		var presigned = presigner
				.presignGetObject(b -> b.getObjectRequest(get).signatureDuration(Duration.ofMinutes(10)));
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(presigned.url().toString())).build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable("id") Long id, Authentication auth) {
		var a = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		UserAccount me = resolveCurrentUser(auth);
		if (me == null || !ownsOrAdmin(me, a)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		s3.deleteObject(b -> b.bucket(bucket).key(a.getS3Key()));
		repo.delete(a);
		return ResponseEntity.noContent().build();
	}

	private boolean ownsOrAdmin(UserAccount me, Attachment a) {
		if (me == null) {
			return false;
		}
		String role = me.getRole();
		boolean admin = role != null && ("ADMIN".equals(role) || "ROLE_ADMIN".equals(role));
		var owner = a.getPost() != null && a.getPost().getUser() != null ? a.getPost().getUser().getId() : null;
		return admin || (owner != null && owner.equals(me.getId()));
	}

	private UserAccount resolveCurrentUser(Authentication auth) {
		if (auth == null) {
			return null;
		}
		var p = auth.getPrincipal();
		if (p instanceof UserDetails ud) {
			return userRepo.findByUsername(ud.getUsername()).orElse(null);
		}
		if (auth instanceof OAuth2AuthenticationToken tok) {
			String provider = tok.getAuthorizedClientRegistrationId();
			String providerId = tok.getName();
			return userRepo.findByProviderAndProviderId(provider, providerId).orElse(null);
		}
		return null;
	}

}
