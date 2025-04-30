package com.team1.mixIt.post.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "내 북마크 목록 페이징 응답")
public class BookmarkResponsePage {

    @Schema(description = "현재 페이지 번호", example = "0")
    private final int page;

    @Schema(description = "페이지 당 사이즈", example = "10")
    private final int size;

    @Schema(description = "전체 페이지 수", example = "5")
    private final int totalPages;

    @Schema(description = "전체 요소 수", example = "42")
    private final long totalElements;

    @Schema(description = "목록 데이터")
    private final List<BookmarkResponse> content;

    public static BookmarkResponsePage from(Page<BookmarkResponse> p) {
        return com.team1.mixIt.post.dto.response.BookmarkResponsePage.builder()
                .page(p.getNumber())
                .size(p.getSize())
                .totalPages(p.getTotalPages())
                .totalElements(p.getTotalElements())
                .content(p.getContent())
                .build();
    }
}

