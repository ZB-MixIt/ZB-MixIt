package com.team1.mixIt.post.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.post.dto.response.LikeResponse;
import com.team1.mixIt.post.service.ReviewLikeService;
import com.team1.mixIt.post.service.ReviewService;
import com.team1.mixIt.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController
@RequestMapping("/api/v1/posts/{postId}/reviews/{reviewId}/like")
@RequiredArgsConstructor
@Tag(name = "리뷰 좋아요", description = "리뷰 좋아요/해제 API")
public class ReviewLikeController {

    private final ReviewLikeService likeService;
    private final ReviewService    reviewService;

    @Operation(summary = "리뷰 좋아요 등록", description = "리뷰에 좋아요를 등록합니다.")
    @ApiResponse(responseCode = "201", description = "좋아요 등록 성공")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseTemplate<LikeResponse> like(
            @PathVariable Long postId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 필요");
        }
        if (!reviewService.existsByIdAndPostId(reviewId, postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰 없음");
        }
        // 좋아요 추가 후, 최신 상태를 반환
        LikeResponse resp = likeService.like(reviewId, user.getId());
        return ResponseTemplate.ok(resp);
    }

    @Operation(summary = "리뷰 좋아요 해제", description = "등록된 좋아요를 취소합니다.")
    @ApiResponse(responseCode = "200", description = "좋아요 해제 성공")
    @DeleteMapping
    public ResponseTemplate<LikeResponse> unlike(
            @PathVariable Long postId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 필요");
        }
        if (!reviewService.existsByIdAndPostId(reviewId, postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰 없음");
        }
        // 좋아요 해제 후, 최신 상태를 반환
        LikeResponse resp = likeService.unlike(reviewId, user.getId());
        return ResponseTemplate.ok(resp);
    }

    @Operation(summary = "리뷰 좋아요 상태 조회", description = "현재 사용자의 좋아요 여부와 총 좋아요 수를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseTemplate<LikeResponse> status(
            @PathVariable Long postId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 필요");
        }
        if (!reviewService.existsByIdAndPostId(reviewId, postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰 없음");
        }
        LikeResponse resp = likeService.status(reviewId, user.getId());
        return ResponseTemplate.ok(resp);
    }
}
