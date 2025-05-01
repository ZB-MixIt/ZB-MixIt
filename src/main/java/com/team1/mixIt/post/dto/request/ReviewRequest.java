package com.team1.mixIt.post.dto.request;

import com.team1.mixIt.common.validation.NoBannedWords;
import com.team1.mixIt.common.validation.NoPersonalInfo;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ReviewRequest {
    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(max = 1000, message = "리뷰 내용은 1000자 이내로 입력해 주세요.")
    @NoBannedWords
    @NoPersonalInfo
    private String content;

    @NotNull
    @DecimalMin("1.0")
    @DecimalMax("5.0")
    private BigDecimal rate;

    private List<@Size(max = 10, message = "이미지 ID는 최대 10개까지 등록될 수 있습니다.") Long> imageIds;
}
