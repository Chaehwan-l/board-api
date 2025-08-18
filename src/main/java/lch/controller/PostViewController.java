package lch.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import lch.entity.Post;
import lch.entity.UserAccount;
import lch.repository.UserAccountRepository;
import lch.service.PostService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostViewController {

    private final PostService postService;
    private final UserAccountRepository userRepo;

    private boolean ownsOrAdmin(UserAccount me, Post p) {
        if (me == null || p == null) {
			return false;
		}
        String role = me.getRole();
        if (role != null && ("ADMIN".equals(role) || "ROLE_ADMIN".equals(role))) {
			return true;
		}
        return p.getUser() != null && me.getId().equals(p.getUser().getId());
    }

    // 목록
    @GetMapping
    public String list(Model model) {
        List<Post> posts = postService.findAll();
        model.addAttribute("posts", posts);
        return "posts/list";
    }

    // 상세
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id,
                         Model model,
                         org.springframework.security.core.Authentication auth) {
        var post = postService.findById(id);
        var me = resolveCurrentUser(auth);
        boolean canEdit = me != null && ownsOrAdmin(me, post);
        model.addAttribute("post", post);
        model.addAttribute("canEdit", canEdit);
        return "posts/detail";
    }

    // 글쓰기 폼
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("post", new Post());
        return "posts/form";
    }

    // 글쓰기 처리 : createBy 사용
    @PostMapping
    public String create(@Valid @ModelAttribute("post") Post post,
                         BindingResult br,
                         Authentication auth) {
        if (br.hasErrors()) {
			return "posts/form";
		}
        UserAccount me = resolveCurrentUser(auth);
        if (me == null) {
			return "redirect:/login";
		}
        postService.createBy(me, post.getTitle(), post.getContent());
        return "redirect:/posts";
    }

    // 수정 폼
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id,
                           Model model,
                           org.springframework.security.core.Authentication auth,
                           org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        lch.entity.Post post = postService.findById(id);
        lch.entity.UserAccount me = resolveCurrentUser(auth); // 기존 helper 사용
        if (me == null || !ownsOrAdmin(me, post)) {
            ra.addFlashAttribute("error", "수정 권한이 없습니다.");
            return "redirect:/posts/{id}";
        }
        model.addAttribute("post", post);
        return "posts/form";
    }

    // 수정 처리 : updateBy 사용
    @PutMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("post") lch.entity.Post post,
                         org.springframework.validation.BindingResult br,
                         org.springframework.security.core.Authentication auth,
                         org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        if (br.hasErrors()) {
			return "posts/form";
		}
        lch.entity.UserAccount me = resolveCurrentUser(auth);
        try {
            postService.updateBy(me, id, post.getTitle(), post.getContent());
            return "redirect:/posts/{id}";
        } catch (org.springframework.security.access.AccessDeniedException e) {
            ra.addFlashAttribute("error", "수정 권한이 없습니다.");
            return "redirect:/posts/{id}";
        }
    }

    // 삭제 처리 : deleteBy 사용
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, Authentication auth) {
        UserAccount me = resolveCurrentUser(auth);
        if (me == null) {
			return "redirect:/login";
		}
        postService.deleteBy(me, id);
        return "redirect:/posts";
    }

    // 현재 사용자 해석
    private UserAccount resolveCurrentUser(Authentication auth) {
        if (auth == null) {
			return null;
		}

        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails ud) {
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
