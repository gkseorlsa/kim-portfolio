package com.example.portfolio_api.service;

import com.example.portfolio_api.domain.Post;
import com.example.portfolio_api.dto.post.PostDetailResponse;
import com.example.portfolio_api.dto.post.PostResponse;
import com.example.portfolio_api.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    // 모든 포스트 조회
    public List<PostResponse> getPosts() {
        return postRepository.findAll(
                // Sort.Direction.DESC: 정렬 방향 지정. 내림차순.
                //      가장 최근에 작성된 글이 맨 위에 오도록 의도한다.

                // createdAt: 정렬할 기준 대상의 이름.
                //      실제 DB 컬럼명 created_at이 아닌, 자바 클래스 필드 변수명 createdAt을 사용해야
                //      JPA가 이를 인식할 수 있다. created_at 컬럼으로 자동 변환한다.
                Sort.by(Sort.Direction.DESC, "createdAt")
        )
                .stream()
                .map(PostResponse::from)
                .toList();
    }

    // slug 로 포스트 조회
    public PostDetailResponse getBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return PostDetailResponse.from(post);
    }

    // 포스트 작성
    public PostDetailResponse post(Post post) {
        Post saved = postRepository.save(post);
        return PostDetailResponse.from(saved);
    }
}
