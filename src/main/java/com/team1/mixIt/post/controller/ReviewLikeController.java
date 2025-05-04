package com.team1.mixIt.post.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.post.service.ReviewLikeService;
import com.team1.mixIt.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/reviews/{reviewId}/like")
@RequiredArgsConstructor
@Tag(name = "리뷰 좋아요", description = "리뷰 좋아요/해제 API")
public class ReviewLikeController {

    private final ReviewLikeService likeService;

    @Operation(summary = "리뷰 좋아요 등록", description = "리뷰에 좋아요를 등록합니다.")
    @ApiResponse(responseCode = "201", description = "좋아요 등록 성공")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseTemplate<Void> like(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user
    ) {
        likeService.like(reviewId, user.getId());
        return ResponseTemplate.ok();
    }

    @Operation(summary = "리뷰 좋아요 해제", description = "등록된 좋아요를 취소합니다.")
    @ApiResponse(responseCode = "200", description = "좋아요 해제 성공")
    @DeleteMapping
    public ResponseTemplate<Void> unlike(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user
    ) {
        likeService.unlike(reviewId, user.getId());
        return ResponseTemplate.ok();
    }
}
