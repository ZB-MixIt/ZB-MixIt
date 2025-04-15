package com.team1.mixIt.post.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "게시판 응답 DTO")
public class PostResponse {

    @Schema(description = "게시물 고유 ID", example = "1")
    private Long id;

    @Schema(description = "작성자 ID", example = "1")
    private Long userId;

    @Schema(description = "카테고리", example = "카페")
    private String category;

    @Schema(description = "게시물 제목", example = "게시물 제목")
    private String title;

    @Schema(description = "게시물 내용", example = "게시물 내용")
    private String content;

    @Schema(description = "조회수", example = "0")
    private Integer viewCount;

    @Schema(description = "좋아요 수", example = "0")
    private Integer likeCount;

    @Schema(description = "북마크 수", example = "0")
    private Integer bookmarkCount;
}
