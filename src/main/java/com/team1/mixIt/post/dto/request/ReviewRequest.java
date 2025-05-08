package com.team1.mixIt.post.dto.request;

import com.team1.mixIt.common.validation.NoBannedWords;
import com.team1.mixIt.common.validation.NoPersonalInfo;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ReviewRequest {
    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(max = 1000, message = "리뷰 내용은 1000자 이내로 입력해 주세요.")
    @NoBannedWords(message = "금지된 단어가 포함되어 있어요")
    @NoPersonalInfo(message = "개인정보가 포함되어 있어요")
    private String content;

    private List<@Size(max = 10, message = "이미지 ID는 최대 10개까지 등록될 수 있습니다.") Long> imageIds;
}
