package com.team1.mixIt.post.dto.response;

import com.team1.mixIt.tag.dto.response.TagStatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import org.springframework.data.domain.Page;
import java.util.List;

@Getter
@Schema(description = "추천 탭용 응답 DTO (당일 북마크 + 인기 태그)")
public class HomeFeedResponse {

    @Schema(description = "당일 북마크 기준 게시물 목록 (페이징)")
    private final Page<PostResponse> posts;

    @Schema(description = "인기 태그 Top10")
    private final List<TagStatResponse> tags;

    public HomeFeedResponse(Page<PostResponse> posts, List<TagStatResponse> tags) {
        this.posts = posts;
        this.tags = tags;
    }
}
