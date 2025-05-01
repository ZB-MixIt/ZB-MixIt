package com.team1.mixIt.post.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.post.dto.request.ReviewRequest;
import com.team1.mixIt.post.dto.response.ReviewResponse;
import com.team1.mixIt.post.service.ReviewService;
import com.team1.mixIt.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/posts/{postId}/reviews")
@RequiredArgsConstructor
@Tag(name = "리뷰 API", description = "게시물 리뷰·평점 관리")
public class ReviewController {

    private final ReviewService svc;

    @Operation(summary = "리뷰 등록", description = "게시물에 리뷰와 평점을 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 등록 성공"),
            @ApiResponse(responseCode = "400", description = "검증 실패"),
            @ApiResponse(responseCode = "404", description = "게시물 없음"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })

    @Validated
    @PostMapping
    public ResponseTemplate<ReviewResponse> create(
            @PathVariable Long postId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ReviewRequest req
    ) {
        return ResponseTemplate.ok(svc.addReview(postId, user, req));
    }

    @Operation(summary = "리뷰 수정", description = "본인이 작성한 리뷰를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 수정 성공"),
            @ApiResponse(responseCode = "400", description = "검증 실패"),
            @ApiResponse(responseCode = "404", description = "리뷰 없음 또는 권한 없음"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @Validated
    @PutMapping("/{reviewId}")
    public ResponseTemplate<ReviewResponse> update(
            @PathVariable Long postId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ReviewRequest req
    ) {
        return ResponseTemplate.ok(svc.updateReview(reviewId, user, req));
    }

    @Operation(summary = "리뷰 삭제", description = "본인이 작성한 리뷰를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "리뷰 없음 또는 권한 없음"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @DeleteMapping("/{reviewId}")
    public ResponseTemplate<Void> delete(
            @PathVariable Long postId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user
    ) {
        svc.deleteReview(reviewId, user);
        return ResponseTemplate.ok();
    }

    @Operation(summary = "리뷰 목록", description = "게시물의 리뷰를 평점 순/최신순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시물 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping
    public ResponseTemplate<List<ReviewResponse>> list(
            @PathVariable Long postId
    ) {
        return ResponseTemplate.ok(svc.listReviews(postId));
    }
}
