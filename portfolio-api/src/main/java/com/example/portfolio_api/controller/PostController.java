package com.example.portfolio_api.controller;

import com.example.portfolio_api.domain.Post;
import com.example.portfolio_api.dto.post.PostCreateeRequest;
import com.example.portfolio_api.dto.post.PostDetailResponse;
import com.example.portfolio_api.dto.post.PostResponse;
import com.example.portfolio_api.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    // 포스트 전체 조회
    @GetMapping
    public List<PostResponse> getPosts() {
        return postService.getPosts();
    }

    // slug로 포스트 조회
    @GetMapping("/{slug}")
    public PostDetailResponse getBySlug(@PathVariable String slug) {
        return postService.getBySlug(slug);
    }

    // 포스트 작성
    @PostMapping
    public PostDetailResponse post(@RequestBody Post post) {
        return postService.post(post);
    }
}
