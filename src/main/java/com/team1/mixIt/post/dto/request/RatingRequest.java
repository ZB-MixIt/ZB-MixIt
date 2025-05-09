package com.team1.mixIt.post.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RatingRequest {
    @NotNull(message = "rate 필드는 필수입니다.")
    @DecimalMin(value = "1.0", message = "최소 1.0 이상이어야 합니다.")
    @DecimalMax(value = "5.0", message = "최대 5.0 이하여야 합니다.")
    private BigDecimal rate;
}

