package com.team1.mixIt.post.dto.response;

import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.entity.PostHashtag;
import com.team1.mixIt.post.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter @Builder
@Schema(description = "게시판 응답 DTO")
public class PostResponse {
    @Schema(description = "게시물 고유 ID", example = "1")
    private Long id;

    @Schema(description = "작성자 ID", example = "1")
    private Long userId;

    @Schema(description = "카테고리", example = "카페")
    private Category category;

    @Schema(description = "게시물 제목", example = "게시물 제목")
    private String title;

    @Schema(description = "게시물 내용", example = "게시물 내용")
    private String content;

    @Schema(description = "첨부 이미지 ID 목록", example = "[1, 2, 3]")
    private List<Long> imageIds;

    @Schema(description = "조회수", example = "0")
    private Integer viewCount;

    @Schema(description = "현재 사용자가 이 게시물을 좋아요한 상태인지", example = "true")
    private Boolean hasLiked;

    @Schema(description = "좋아요 수", example = "0")
    private Long likeCount;

    @Schema(description = "북마크 수", example = "0")
    private Integer bookmarkCount;

    @Schema(description = "게시물 태그 목록")
    private List<String> tags;

    @Schema(description = "대표 이미지 URL (없으면 기본 이미지)", example = "https://../기본이미지.png")
    private String defaultImageUrl;

    @Schema(description = "현재 사용자가 작성자인지 여부", example = "true")
    private Boolean isAuthor;

    public static PostResponse fromEntity(Post p, Long currentUserId, String defaultImgUrl) {
        boolean authorFlag = currentUserId != null && p.getUserId().equals(currentUserId);
        return PostResponse.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .category(p.getCategory())
                .title(p.getTitle())
                .content(p.getContent())
                .imageIds(p.getImageIds())
                .viewCount(p.getViewCount())
                .bookmarkCount(p.getBookmarkCount())
                .hasLiked(false)
                .likeCount(0L)
                .tags(p.getHashtag().stream().map(PostHashtag::getHashtag).toList())
                .defaultImageUrl(p.getImageIds().isEmpty() ? defaultImgUrl : null)
                .isAuthor(authorFlag)
                .build();
    }
}
