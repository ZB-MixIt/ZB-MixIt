package com.team1.mixIt.tag.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "인기 태그 응답 DTO")
public class TagStatResponse {
    @Schema(description = "태그 이름", example = "스타벅스")
    private final String tag;

    @Schema(description = "해당 기간 내 사용 횟수", example = "42")
    private final Long useCount;
}
