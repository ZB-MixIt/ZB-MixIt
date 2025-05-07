package com.team1.mixIt.post.service;

import com.team1.mixIt.post.dto.response.LikeResponse;
import com.team1.mixIt.post.entity.Review;
import com.team1.mixIt.post.entity.ReviewLike;
import com.team1.mixIt.post.repository.ReviewLikeRepository;
import com.team1.mixIt.post.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewLikeService {
    private final ReviewLikeRepository repo;

    @Transactional
    public LikeResponse like(Long reviewId, Long userId) {
        boolean added = false;
        if (!repo.existsByReviewIdAndUserId(reviewId, userId)) {
            repo.save(new ReviewLike(reviewId, userId));
            added = true;
        }
        long count = repo.countByReviewId(reviewId);
        return new LikeResponse(added, count);
    }

    @Transactional
    public LikeResponse unlike(Long reviewId, Long userId) {
        if (repo.existsByReviewIdAndUserId(reviewId, userId)) {
            repo.deleteByReviewIdAndUserId(reviewId, userId);
        }
        long count = repo.countByReviewId(reviewId);
        return new LikeResponse(false, count);
    }

    public LikeResponse status(Long reviewId, Long userId) {
        boolean hasLiked = repo.existsByReviewIdAndUserId(reviewId, userId);
        long count = repo.countByReviewId(reviewId);
        return new LikeResponse(hasLiked, count);
    }
}