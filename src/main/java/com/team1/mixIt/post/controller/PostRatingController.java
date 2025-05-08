package com.team1.mixIt.post.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.post.service.PostRatingService;
import com.team1.mixIt.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts/{postId}/rate")
@Tag(name = "게시물 평점 API", description = "게시물에 대한 평점 등록·조회")
public class PostRatingController {

    private final PostRatingService ratingService;

    @Operation(
            summary = "게시물 별점 등록/수정",
            description = "로그인한 사용자가 게시물에 1.0~5.0 사이의 평점을 등록하거나, 이미 등록한 평점이 있으면 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "별점 등록/수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력(범위 벗어남 등)"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "게시물 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping
    public ResponseTemplate<Void> ratePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal User user,
            @RequestParam BigDecimal rate
    ) {
        if (rate == null) {
            rate = BigDecimal.ZERO;
        }
        ratingService.addOrUpdateRating(postId, user.getId(), rate);
        return ResponseTemplate.ok();
    }

    @Operation(
            summary = "내 별점 조회",
            description = "로그인한 사용자가 해당 게시물에 매긴 평점을 조회합니다. 등록된 평점이 없으면 0을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "게시물 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping
    public ResponseTemplate<BigDecimal> getMyRate(
            @PathVariable Long postId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseTemplate.ok(ratingService.getUserRating(postId, user.getId()));
    }
}
