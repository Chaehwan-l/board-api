package lch.controller;

import java.util.List;

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
import lch.service.PostService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostViewController {

    private final PostService postService;

    // 1) 목록 조회
    @GetMapping
    public String list(Model model) {
        List<Post> posts = postService.findAll();
        model.addAttribute("posts", posts);
        return "posts/list";
    }

    // 2) 상세 조회
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Post post = postService.findById(id);
        model.addAttribute("post", post);
        return "posts/detail";
    }

    // 3) 글쓰기 폼
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("post", new Post());
        return "posts/form";
    }

    // 4) 글쓰기 처리
    @PostMapping
    public String create(@Valid @ModelAttribute Post post, BindingResult br) {
        if (br.hasErrors()) {
            return "posts/form";
        }
        postService.create(post);
        return "redirect:/posts";
    }

    // 5) 수정 폼
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Post post = postService.findById(id);
        model.addAttribute("post", post);
        return "posts/form";
    }

    // 6) 수정 처리
    @PutMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute Post post,
                         BindingResult br) {
        if (br.hasErrors()) {
            return "posts/form";
        }
        postService.update(id, post);
        return "redirect:/posts/{id}";
    }

    // 7) 삭제 처리 (선택)
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        postService.delete(id);
        return "redirect:/posts";
    }
}
