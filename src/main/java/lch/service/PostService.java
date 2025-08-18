package lch.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lch.entity.Post;
import lch.entity.UserAccount;
import lch.repository.PostRepository;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repo;

    public PostService(PostRepository repo) { this.repo = repo; }

    public List<Post> findAll() { return repo.findAll(); }

    public Post findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found: " + id));
    }

    @Transactional
    public Post createBy(UserAccount user, String title, String content) {
        if (user == null) {
			throw new AccessDeniedException("LOGIN_REQUIRED");
		}
        Post p = new Post();
        p.setTitle(title);
        p.setContent(content);
        p.setUser(user);              // 작성자 고정
        return repo.save(p);          // createdAt은 @PrePersist
    }

    @Transactional
    public Post updateBy(UserAccount user, Long id, String title, String content) {
        Post p = repo.findById(id).orElseThrow(() -> new RuntimeException("Post not found: " + id));
        if (!ownsOrAdmin(user, p)) {
			throw new AccessDeniedException("FORBIDDEN");
		}
        p.setTitle(title);
        p.setContent(content);
        return p;                     // JPA dirty checking
    }

    @Transactional
    public void deleteBy(UserAccount user, Long id) {
        Post p = repo.findById(id).orElseThrow(() -> new RuntimeException("Post not found: " + id));
        if (!ownsOrAdmin(user, p)) {
			throw new AccessDeniedException("FORBIDDEN");
		}
        repo.delete(p);
    }

    private boolean ownsOrAdmin(UserAccount u, Post p) {
        if (u == null) {
			return false;
		}
        String role = u.getRole();
        if (role != null && ("ADMIN".equals(role) || "ROLE_ADMIN".equals(role))) {
			return true;
		}
        return p.getUser() != null && u.getId().equals(p.getUser().getId());
    }
}
