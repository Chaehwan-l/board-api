package lch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import lch.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPost_IdOrderByIdAsc(Long postId);
    long countByPost_Id(Long postId);
}
