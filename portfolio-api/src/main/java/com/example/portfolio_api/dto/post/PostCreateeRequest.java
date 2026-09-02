package com.example.portfolio_api.dto.post;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostCreateeRequest {
    private String slug;
    private String title;
    private String summary;
    private String content;
}

