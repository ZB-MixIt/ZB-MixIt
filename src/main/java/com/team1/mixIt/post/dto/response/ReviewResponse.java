package com.team1.mixIt.post.dto.response;

import com.team1.mixIt.post.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리뷰 응답 DTO")
public class ReviewResponse {
    @Schema(description = "리뷰 고유 ID", example = "123")
    private Long id;

    @Schema(description = "작성자 ID", example = "45")
    private Long userId;

    @Schema(description = "작성자 닉네임", example = "test")
    private String userNickname;

    @Schema(description = "리뷰 내용", example = "정말 맛있어요!")
    private String content;

    @Schema(description = "별점", example = "4.5")
    private BigDecimal rate;

    @Schema(description = "작성 시각")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각")
    private LocalDateTime modifiedAt;

    @Schema(description = "첨부 이미지 ID 목록")
    private List<Long> imageIds;

    @Schema(description = "현재 사용자가 작성자인지 여부", example = "true")
    private Boolean isAuthor;


    public static ReviewResponse fromEntity(Review r, Long currentUserId) {
        boolean authorFlag = (currentUserId != null)
                && r.getUser().getId().equals(currentUserId);

        return ReviewResponse.builder()
                .id(r.getId())
                .userId(r.getUser().getId())
                .userNickname(r.getUser().getNickname())
                .content(r.getContent())
                .rate(r.getRate())
                .createdAt(r.getCreatedAt())
                .modifiedAt(r.getModifiedAt())
                .imageIds(r.getImageIds())
                .isAuthor(authorFlag)
                .build();
    }
}
