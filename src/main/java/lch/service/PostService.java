package lch.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lch.entity.Post;
import lch.repository.PostRepository;

@Service
public class PostService {

    private final PostRepository repo;

    public PostService(PostRepository repo) {
        this.repo = repo;
    }

    public List<Post> findAll() {
        return repo.findAll();
    }

    public Post findById(Long id) {
        return repo.findById(id)
                   .orElseThrow(() -> new RuntimeException("Post not found: " + id));
    }

    public Post create(Post post) {
        return repo.save(post);
    }

    public Post update(Long id, Post post) {
        post.setId(id);
        return repo.save(post);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
