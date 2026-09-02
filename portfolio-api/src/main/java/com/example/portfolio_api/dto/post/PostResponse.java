package com.example.portfolio_api.dto.post;

import com.example.portfolio_api.domain.Post;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String slug;
    private String title;
    private String summary;

    // 프론트 컬럼명은 snake_case, 자바 필드명은 camelCase로써 작성 방식이 다른데,
    // 프론트에 맞춰주기 위해 JsonProperty를 사용한다.
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    // Builder 어노테이션을 사용하는 대신, 직접 생성자 호출 방식을 사용했다.
    // DTO 변환 목적은 Post 엔티티를 받아서 프론트엔드가 요구하는 JSON 포멧대로 1:1 복사하는 것이다.
    // 모든 필드가 필수값이고, 필드 개수가 적게(5개) 정형화되어 있는 DTO 변환에서는 @Builder를 도입하는 대신
    //      메서드 안에서 생성자를 호출하는 방식이 관례 중 하나이기도 하다.

    // @Builder 방식도 물론 좋은 방법이다. 필드 이름들이 명시적으로 드러나므로 가독성이 좋으며,
    //      매개변수끼리 이름으로 매핑하므로 실수가 원천 차단된다. 또한 일부 필드를 null로 설정이 가능하다.
    // 그러나 현재 생성자 호출 방식은 인자 개수가 맞지 않을 경우 즉시 컴파일 에러가 발생해, 사고가 날 가능성을
    // 미연에 방지할 수 있다는 장점이 있다. 모든 필드가 반드시 꽉 채워져야 하는 1:1 단순 변환 DTO일 경우
    // 해당 방식이 더 유리한 경우도 있다.
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getSummary(),
                post.getCreatedAt()
        );
    }
}
