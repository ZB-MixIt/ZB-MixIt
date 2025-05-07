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
    private final ReviewLikeRepository likeRepository;
    private final ReviewRepository reviewRepository;


    @Transactional
    public void like(Long reviewId, Long userId) {
        if (likeRepository.existsByReviewIdAndUserId(reviewId, userId)) return;
        likeRepository.save(new ReviewLike(reviewId, userId));
    }

    @Transactional
    public void unlike(Long reviewId, Long userId) {
        likeRepository.deleteByReviewIdAndUserId(reviewId, userId);
    }

    @Transactional(readOnly = true)
    public LikeResponse status(Long reviewId, Long userId) {
        boolean hasLiked = likeRepository.existsByReviewIdAndUserId(reviewId, userId);
        long count = reviewRepository.findById(reviewId)
                .map(Review::getLikeCount)
                .orElse(0L);
        return new LikeResponse(hasLiked, count);
    }
}
