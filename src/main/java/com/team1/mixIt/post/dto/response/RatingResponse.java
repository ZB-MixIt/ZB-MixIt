package com.team1.mixIt.post.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "게시물 평점 정보")
public class RatingResponse {
    @Schema(description="평균 별점", example="4.3")
    private BigDecimal averageRating;

    @Schema(description="별점 참여자 수", example="12")
    private long ratingCount;
}
