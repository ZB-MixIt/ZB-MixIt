package com.team1.mixIt.post.dto.request;

import com.team1.mixIt.post.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(description = "게시물 검색 및 필터 요청")
public class PostSearchRequest {
    @Schema(description = "검색 키워드 (제목+태그)", example = "서브웨이")
    private String keyword;

    @Schema(description = "카테고리 필터", example = "CAFE")
    private Category category;

    @Schema(description = "정렬 기준 (createdAt 또는 views7d)", example = "createdAt")
    private String sortBy = "createdAt";

    @Schema(description = "정렬 방향 (asc/desc)", example = "desc")
    private String sortDir = "desc";

    @Schema(description = "페이지 번호 (0부터)", example = "0")
    private int page = 0;

    @Schema(description = "페이지 크기", example = "20")
    private int size = 20;
}
