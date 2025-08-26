package lch.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import lch.entity.Post;
import lch.entity.UserAccount;
import lch.repository.AttachmentRepository;
import lch.repository.UserAccountRepository;
import lch.service.AttachmentService;
import lch.service.CommentService;
import lch.service.PostService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostViewController {

	private final PostService postService;
	private final CommentService commentService;
	private final AttachmentService attachmentService;
	private final UserAccountRepository userRepo;
	private final AttachmentRepository attachmentRepository;


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
	public String list(@RequestParam(name = "page", defaultValue = "0") int page,
					   @RequestParam(name = "size", defaultValue = "10") int size,
					   @RequestParam(name = "q", required = false) String q,
					   @RequestParam(name = "type", defaultValue = "title") String type, Model model) {

		// 방어적 한도
		int p = Math.max(page, 0);
		int s = Math.min(Math.max(size, 1), 100);

		Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "id"));
		Page<Post> pageData = postService.search(type, q, pageable);

		model.addAttribute("page", pageData);
		model.addAttribute("posts", pageData.getContent());

		// 번호 계산용
		model.addAttribute("pageIndex", pageData.getNumber());
		model.addAttribute("pageSize", pageData.getSize());
		model.addAttribute("totalElements", pageData.getTotalElements());

		// 검색 유지
		model.addAttribute("q", q == null ? "" : q);
		model.addAttribute("type", type);

		return "posts/list";
	}

	// 상세
	@GetMapping("/{id}")
	public String detail(@PathVariable("id") Long id, Model model, Authentication auth) {
		var post = postService.findById(id);
		var me = resolveCurrentUser(auth);
		boolean canEdit = me != null && ownsOrAdmin(me, post);
		model.addAttribute("post", post);
		model.addAttribute("canEdit", canEdit);
		model.addAttribute("attachments", attachmentRepository.findByPostIdOrderByIdAsc(id));
		model.addAttribute("comments", commentService.list(id));
		return "posts/detail";
	}

	// 글쓰기 폼
	@GetMapping("/new")
	public String createForm(Model model) {
		model.addAttribute("post", new Post());
		return "posts/form";
	}

	// 글쓰기 처리 + 드래프트 정리
	@PostMapping
	public String create(@Valid @ModelAttribute("post") Post post, BindingResult br, Authentication auth,
			@RequestParam(value = "draftId", required = false) String draftId) {
		if (br.hasErrors()) {
			return "posts/form";
		}
		UserAccount me = resolveCurrentUser(auth);
		if (me == null) {
			return "redirect:/login";
		}

		// createBy가 Post를 반환하도록 가정
		Post saved = postService.createBy(me, post.getTitle(), post.getContent());

		if (draftId != null && !draftId.isBlank()) {
			attachmentService.finalizeDraftToPost(draftId, saved);
		}
		return "redirect:/posts/" + saved.getId();
	}

	// 수정 폼
	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable("id") Long id, Model model, Authentication auth,
			org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
		Post post = postService.findById(id);
		UserAccount me = resolveCurrentUser(auth);
		if (me == null || !ownsOrAdmin(me, post)) {
			ra.addFlashAttribute("error", "수정 권한이 없습니다.");
			return "redirect:/posts/{id}";
		}
		model.addAttribute("post", post);
		model.addAttribute("attachments", attachmentRepository.findByPostIdOrderByIdAsc(id));
		return "posts/form";
	}

	// 수정 처리 + 드래프트 정리
	@PutMapping("/{id}")
	public String update(@PathVariable("id") Long id, @Valid @ModelAttribute("post") Post post, BindingResult br,
			Authentication auth, @RequestParam(value = "draftId", required = false) String draftId,
			org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
		if (br.hasErrors()) {
			return "posts/form";
		}
		UserAccount me = resolveCurrentUser(auth);
		try {
			postService.updateBy(me, id, post.getTitle(), post.getContent());
			if (draftId != null && !draftId.isBlank()) {
				Post updated = postService.findById(id);
				attachmentService.finalizeDraftToPost(draftId, updated);
			}
			return "redirect:/posts/{id}";
		} catch (org.springframework.security.access.AccessDeniedException e) {
			ra.addFlashAttribute("error", "수정 권한이 없습니다.");
			return "redirect:/posts/{id}";
		}
	}

	// 삭제
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
