package lch.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lch.entity.UserAccount;
import lch.repository.UserAccountRepository;
import lch.service.CommentService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final UserAccountRepository userRepo;

    @PostMapping("/posts/{postId}/comments")
    public String create(@PathVariable("postId") Long postId,
                         @RequestParam("content") String content,
                         Authentication auth) {
        UserAccount me = resolve(auth);
        commentService.add(me, postId, content);
        return "redirect:/posts/" + postId + "#comments";
    }

    // 폼에서 _method=delete 로 들어오는 경우 처리
    @PostMapping(value="/comments/{id}", params="_method=delete")
    public String deleteViaForm(@PathVariable("id") Long id,
                                @RequestParam("postId") Long postId,
                                Authentication auth) {
        UserAccount me = resolve(auth);
        commentService.delete(me, id);
        return "redirect:/posts/" + postId + "#comments";
    }

    @DeleteMapping("/comments/{id}")
    public String delete(@PathVariable("id") Long id,
                         @RequestParam("postId") Long postId,
                         Authentication auth) {
        UserAccount me = resolve(auth);
        commentService.delete(me, id);
        return "redirect:/posts/" + postId + "#comments";
    }

    private UserAccount resolve(Authentication auth) {
        if (auth == null) {
			return null;
		}
        Object p = auth.getPrincipal();
        if (p instanceof UserDetails ud) {
			return userRepo.findByUsername(ud.getUsername()).orElse(null);
		}
        if (auth instanceof OAuth2AuthenticationToken tok) {
			return userRepo.findByProviderAndProviderId(
                tok.getAuthorizedClientRegistrationId(), tok.getName()).orElse(null);
		}
        return null;
    }
}
