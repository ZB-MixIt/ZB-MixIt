// src/main/java/com/team1/mixIt/post/controller/PostLikeController.java
package com.team1.mixIt.post.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.post.dto.response.LikeResponse;
import com.team1.mixIt.post.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts/{postId}/like")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService likeService;

    @PostMapping
    public ResponseTemplate<LikeResponse> toggle(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return likeService.toggleLike(postId, userId);
    }

    @GetMapping
    public ResponseTemplate<LikeResponse> status(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return likeService.status(postId, userId);
    }
}
