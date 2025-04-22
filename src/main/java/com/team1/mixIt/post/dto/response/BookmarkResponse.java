package com.team1.mixIt.post.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "내 북마크 게시물 응답 DTO")
public class BookmarkResponse {

    @Schema(description = "게시물 고유 ID", example = "42")
    private final Long id;

    @Schema(description = "게시물 제목", example = "서브웨이 꿀조합")
    private final String title;

    @Schema(description = "첨부 이미지 ID 목록", example = "[1,2,3]")
    private final List<Long> imageIds;

    @Schema(description = "북마크 수", example = "17")
    private final Integer bookmarkCount;

    public static BookmarkResponse fromEntity(com.team1.mixIt.post.entity.Post post) {
        return BookmarkResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .imageIds(post.getImageIds())
                .bookmarkCount(post.getBookmarkCount())
                .build();
    }
}
