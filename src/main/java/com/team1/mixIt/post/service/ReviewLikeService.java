package com.team1.mixIt.post.service;

import com.team1.mixIt.post.entity.ReviewLike;
import com.team1.mixIt.post.repository.ReviewLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewLikeService {
    private final ReviewLikeRepository likeRepository;

    @Transactional
    public void like(Long reviewId, Long userId) {
        if (likeRepository.existsByReviewIdAndUserId(reviewId, userId)) return;
        likeRepository.save(new ReviewLike(reviewId, userId));
    }

    @Transactional
    public void unlike(Long reviewId, Long userId) {
        likeRepository.deleteByReviewIdAndUserId(reviewId, userId);
    }
}

