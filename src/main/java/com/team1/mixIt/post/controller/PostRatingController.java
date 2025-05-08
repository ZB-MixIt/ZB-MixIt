package com.team1.mixIt.post.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.post.service.PostRatingService;
import com.team1.mixIt.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts/{postId}/rate")
public class PostRatingController {

    private final PostRatingService ratingService;

    @PostMapping
    public ResponseTemplate<Void> ratePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal User user,
            @RequestParam BigDecimal rate
    ) {
        ratingService.addOrUpdateRating(postId, user.getId(), rate);
        return ResponseTemplate.ok();
    }

    @GetMapping
    public ResponseTemplate<BigDecimal> getMyRate(
            @PathVariable Long postId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseTemplate.ok(ratingService.getUserRating(postId, user.getId()));
    }
}
