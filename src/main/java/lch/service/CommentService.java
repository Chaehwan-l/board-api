package lch.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lch.entity.Comment;
import lch.entity.Post;
import lch.entity.UserAccount;
import lch.repository.CommentRepository;
import lch.repository.PostRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository comments;
    private final PostRepository posts;

    public List<Comment> list(Long postId) {
        return comments.findByPost_IdOrderByIdAsc(postId);
    }

    @Transactional
    public Comment add(UserAccount me, Long postId, String content) {
        if (me == null) {
			throw new AccessDeniedException("LOGIN_REQUIRED");
		}
        Post post = posts.findById(postId).orElseThrow();
        Comment c = new Comment();
        c.setPost(post);
        c.setUser(me);
        c.setContent(content);
        return comments.save(c);
    }

    @Transactional
    public void delete(UserAccount me, Long commentId) {
        Comment c = comments.findById(commentId).orElseThrow();
        if (!ownsOrAdmin(me, c)) {
			throw new AccessDeniedException("FORBIDDEN");
		}
        comments.delete(c);
    }

    private boolean ownsOrAdmin(UserAccount u, Comment c) {
        if (u == null) {
			return false;
		}
        String role = u.getRole();
        if (role != null && ("ADMIN".equals(role) || "ROLE_ADMIN".equals(role))) {
			return true;
		}
        return c.getUser() != null && u.getId().equals(c.getUser().getId());
    }
}
