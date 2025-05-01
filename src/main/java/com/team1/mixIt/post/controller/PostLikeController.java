package com.team1.mixIt.post.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.post.dto.response.LikeResponse;
import com.team1.mixIt.post.service.PostLikeService;
import com.team1.mixIt.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/posts/{postId}/like")
@RequiredArgsConstructor
@Tag(name = "좋아요 API", description = "게시물 좋아요/상태 조회")
public class PostLikeController {

    private final PostLikeService likeService;

    @Operation(summary = "게시물 좋아요 등록", description = "게시물에 좋아요를 남깁니다. + action_log에 LIKE 이벤트 기록")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "좋아요 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "게시물 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseTemplate<LikeResponse> createLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal User user
    ) {
        LikeResponse resp = likeService.addLike(postId, user.getId());
        return ResponseTemplate.ok(resp);
    }

    @Operation(summary = "게시물 좋아요 해제",
            description = "게시물에 남긴 좋아요를 취소합니다 + action_log에 UNLIKE 이벤트 기록")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "좋아요 해제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "게시물 또는 좋아요 기록 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseTemplate<Void> deleteLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal User user
    ) {
        likeService.removeLike(postId, user.getId());
        return ResponseTemplate.ok();
    }

    @Operation(summary = "게시물 좋아요 상태 조회", description = "현재 사용자의 좋아요 여부와 카운트를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "게시물 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping
    public ResponseTemplate<LikeResponse> status(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId
    ) {
        LikeResponse resp = likeService.status(postId, user.getId());
        return ResponseTemplate.ok(resp);
    }
}
