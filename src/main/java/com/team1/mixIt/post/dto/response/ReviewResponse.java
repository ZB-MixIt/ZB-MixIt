package com.team1.mixIt.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
}
