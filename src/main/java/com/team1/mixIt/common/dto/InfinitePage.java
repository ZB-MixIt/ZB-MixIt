package com.team1.mixIt.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Schema(description = "무한스크롤용 페이지 DTO")
public class InfinitePage<T> {

    @Schema(description = "현재 페이지 번호 (0부터 시작)")
    private int page;

    @Schema(description = "페이지 크기")
    private int size;

    @Schema(description = "전체 페이지 수")
    private int totalPages;

    @Schema(description = "전체 요소 수")
    private long totalElements;

    @Schema(description = "내용 리스트")
    private List<T> content;

    @Schema(description = "비어있을 때 보여줄 메시지")
    private String emptyMessage;

    @Schema(description = "다음에 불러올 페이지 (없으면 null)")
    private Integer nextPage;
}
