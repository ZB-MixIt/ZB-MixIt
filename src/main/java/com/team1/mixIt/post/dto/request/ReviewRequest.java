package com.team1.mixIt.post.dto.request;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewRequest {
    @NotBlank
    @Size(max=1000)
    private String content;

    @NotNull
    @DecimalMin("1.0")
    @DecimalMax("5.0")
    private BigDecimal rate;

    private List<@Size(max=10) Long> imageIds;
}
