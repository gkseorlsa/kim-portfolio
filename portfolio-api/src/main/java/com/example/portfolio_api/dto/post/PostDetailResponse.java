package com.example.portfolio_api.dto.post;

import com.example.portfolio_api.domain.Post;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PostDetailResponse {
    private Long id;
    private String slug;
    private String title;
    private String summary;
    private String content;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static PostDetailResponse from(Post post) {
        return new PostDetailResponse(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getSummary(),
                post.getContent(),
                post.getCreatedAt()
        );
    }
}
