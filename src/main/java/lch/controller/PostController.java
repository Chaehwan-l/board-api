package lch.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lch.entity.Post;
import lch.service.PostService;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService service;

    public PostController(PostService service) {
        this.service = service;
    }

    @GetMapping
    public List<Post> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Post detail(@PathVariable("id") Long id) {
        return service.findById(id);
    }

    @PostMapping
    public Post create(@RequestBody Post post) {
        return service.create(post);
    }

    @PutMapping("/{id}")
    public Post update(@PathVariable("id") Long id, @RequestBody Post post) {
        return service.update(id, post);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        service.delete(id);
    }
}
