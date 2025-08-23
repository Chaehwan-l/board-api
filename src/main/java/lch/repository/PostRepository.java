package lch.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import lch.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 제목 부분 일치 검색 (대소문자 무시)
    Page<Post> findByTitleContainingIgnoreCase(String q, Pageable pageable);

    // 작성자(username) 부분 일치 검색 (대소문자 무시)
    Page<Post> findByUser_UsernameContainingIgnoreCase(String q, Pageable pageable);
}
