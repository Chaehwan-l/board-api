package lch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import lch.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 제목 검색 기능
    List<Post> findByTitleContaining(String keyword);
}
