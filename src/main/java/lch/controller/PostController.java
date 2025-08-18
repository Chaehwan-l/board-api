package lch.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lch.dto.PostCreateRequest;
import lch.dto.PostUpdateRequest;
import lch.entity.Post;
import lch.entity.UserAccount;
import lch.repository.UserAccountRepository;
import lch.service.PostService;

@RestController
@RequestMapping("/api/secure/posts")
public class PostController {

    private final PostService postService;
    private final UserAccountRepository userRepo;

    public PostController(PostService postService, UserAccountRepository userRepo) {
        this.postService = postService;
        this.userRepo = userRepo;
    }

    @PostMapping
    public Post create(@Valid @RequestBody PostCreateRequest req, Authentication auth) {
        UserAccount me = resolveCurrentUser(auth);
        return postService.createBy(me, req.title(), req.content());
    }

    @PutMapping("/{id}")
    public Post update(@PathVariable Long id, @Valid @RequestBody PostUpdateRequest req, Authentication auth) {
        UserAccount me = resolveCurrentUser(auth);
        return postService.updateBy(me, id, req.title(), req.content());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        UserAccount me = resolveCurrentUser(auth);
        postService.deleteBy(me, id);
    }

    private UserAccount resolveCurrentUser(Authentication auth) {
        if (auth == null) {
			return null;
		}

        // 폼 로그인
        if (auth.getPrincipal() instanceof UserDetails ud) {
            return userRepo.findByUsername(ud.getUsername()).orElse(null);
        }

        // OAuth2 로그인
        if (auth instanceof OAuth2AuthenticationToken tok) {
            String provider = tok.getAuthorizedClientRegistrationId();
            String providerId = tok.getName();
            return userRepo.findByProviderAndProviderId(provider, providerId).orElse(null);
        }
        return null;
    }
}
