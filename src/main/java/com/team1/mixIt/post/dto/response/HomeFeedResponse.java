package com.team1.mixIt.post.dto.response;

import com.team1.mixIt.common.dto.InfinitePage;
import com.team1.mixIt.tag.dto.response.TagStatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
@Getter
@Schema(description = "추천 탭용 응답 DTO (당일 북마크 + 인기 태그)")
public class HomeFeedResponse {
    private final InfinitePage<PostResponse> posts;
    private final List<TagStatResponse> tags;

    // 기존 생성자 (InfinitePage 직접 주입용)
    public HomeFeedResponse(InfinitePage<PostResponse> posts, List<TagStatResponse> tags) {
        this.posts = posts;
        this.tags  = tags;
    }

    // 새로 추가 생성자: Page<PostResponse> 를 받아 내부에서 InfinitePage 로 변환
    public HomeFeedResponse(Page<PostResponse> page, List<TagStatResponse> tags) {
        this.posts = new InfinitePage<>(
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getContent(),
                page.hasContent() ? null : "게시물이 없습니다",
                page.hasNext()   ? page.getNumber() + 1 : null
        );
        this.tags = tags;
    }
}
