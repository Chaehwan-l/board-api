package lch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import lch.entity.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
	List<Attachment> findByPostIdOrderByIdAsc(Long postId);
}
