package com.team1.mixIt.post.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.post.dto.response.LikeResponse;
import com.team1.mixIt.post.service.PostLikeService;
import com.team1.mixIt.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts/{postId}/like")
@RequiredArgsConstructor
@Tag(name = "좋아요 API", description = "게시물 좋아요/상태 조회")
public class PostLikeController {

    private final PostLikeService likeService;

    @Operation(summary = "게시물 좋아요 토글", description = "좋아요/취소를 토글합니다.")
    @ApiResponse(responseCode = "200", description = "토글 완료")
    @PostMapping("/{postId}/like")
    public ResponseTemplate<LikeResponse> toggle(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId
    ) {
        return likeService.toggleLike(postId, user.getId());
    }

    @Operation(summary = "게시물 좋아요 상태 조회", description = "현재 사용자의 좋아요 여부와 카운트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{postId}/like")
    public ResponseTemplate<LikeResponse> status(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId
    ) {
        return likeService.status(postId, user.getId());
    }
}
