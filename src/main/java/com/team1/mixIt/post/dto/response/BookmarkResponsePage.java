package com.team1.mixIt.post.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter @Setter
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

    @Schema(description = "빈 목록일 때 프론트에 보여줄 메시지 ==> 추후 변경?", example = "더 많은 조합 보러가기")
    private String emptyMessage;

    public static BookmarkResponsePage from(Page<BookmarkResponse> p) {
        return BookmarkResponsePage.builder()
                .page(p.getNumber())
                .size(p.getSize())
                .totalPages(p.getTotalPages())
                .totalElements(p.getTotalElements())
                .content(p.getContent())
                .emptyMessage(null)
                .build();
    }
}
