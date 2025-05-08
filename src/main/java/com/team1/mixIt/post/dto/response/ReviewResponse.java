package com.team1.mixIt.post.dto.response;

import com.team1.mixIt.image.service.ImageService;
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

    @Schema(description = "작성 시각")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각")
    private LocalDateTime modifiedAt;

    @Schema(description = "첨부 이미지 목록", implementation = ImageDto.class)
    private List<ImageDto> images;

    @Schema(description = "현재 사용자가 작성자인지 여부", example = "true")
    private Boolean isAuthor;

    @Getter @Setter
    @Schema(description = "이미지 DTO")
    public static class ImageDto {
        @Schema(description = "이미지 고유 ID", example = "1")
        private Long id;

        @Schema(description = "이미지 URL", example = "https://cdn.example.com/img1.png")
        private String src;

        public ImageDto(Long id, String src) {
            this.id = id;
            this.src = src;
        }
    }

    public static ReviewResponse fromEntity(
            Review r,
            Long currentUserId,
            ImageService imageService
    ) {
        boolean authorFlag = currentUserId != null
                && r.getUser().getId().equals(currentUserId);

        List<ImageDto> dtoList = r.getImageIds().stream()
                .map(imageService::findById)
                .map(img -> new ImageDto(
                        img.getId(),
                        img.getUrl()
                ))
                .toList();

        return ReviewResponse.builder()
                .id(r.getId())
                .userId(r.getUser().getId())
                .userNickname(r.getUser().getNickname())
                .content(r.getContent())
                .createdAt(r.getCreatedAt())
                .modifiedAt(r.getModifiedAt())
                .images(dtoList)
                .isAuthor(authorFlag)
                .build();
    }
}
