package com.team1.mixIt.post.dto.response;

import com.team1.mixIt.image.entity.Image;
import com.team1.mixIt.post.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long userId;
    private String userNickname;
    private String content;
    private BigDecimal rate;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private List<Long> imageIds;

    public static ReviewResponse fromEntity(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .userId(r.getUser().getId())
                .userNickname(r.getUser().getNickname())
                .content(r.getContent())
                .rate(r.getRate())
                .createdAt(r.getCreatedAt())
                .modifiedAt(r.getModifiedAt())
                .imageIds(
                        r.getImages().stream()
                                .map(Image::getId)
                                .collect(Collectors.toList())
                )
                .build();
    }
}