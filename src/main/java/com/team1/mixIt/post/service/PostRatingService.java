package com.team1.mixIt.post.service;

import com.team1.mixIt.post.dto.response.RatingResponse;
import com.team1.mixIt.post.entity.PostRating;
import com.team1.mixIt.post.repository.PostRatingRepository;
import com.team1.mixIt.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostRatingService {
    private final PostRatingRepository ratingRepo;
    private final PostRepository postRepo;

    @Transactional
    public void addOrUpdateRating(Long postId, Long userId, BigDecimal rate) {
        PostRating rating = ratingRepo.findByPostIdAndUserId(postId, userId)
                .map(r -> {
                    r.setRate(rate);
                    return r;
                })
                .orElse(PostRating.builder()
                        .postId(postId)
                        .userId(userId)
                        .rate(rate)
                        .build());

        ratingRepo.save(rating);
        updatePostAvgRating(postId);
    }

    public BigDecimal getUserRating(Long postId, Long userId) {
        return ratingRepo.findByPostIdAndUserId(postId, userId)
                .map(PostRating::getRate)
                .orElse(BigDecimal.ZERO);
    }

    private void updatePostAvgRating(Long postId) {
        BigDecimal avg = ratingRepo.findAverageRateByPostId(postId);
        postRepo.findById(postId).ifPresent(post -> {
            post.setAvgRating(avg.doubleValue());
        });
    }

    public BigDecimal getAverageRate(Long postId) {
        return Optional.ofNullable(ratingRepo.findAverageRateByPostId(postId))
                .orElse(BigDecimal.ZERO)
                .setScale(1, RoundingMode.HALF_UP);
    }

    public long getRatingCount(Long postId) {
        return ratingRepo.countByPostId(postId);
    }

    @Transactional(readOnly = true)
    public RatingResponse getRatingResponse(Long postId) {
        BigDecimal avg = getAverageRate(postId);
        long count = getRatingCount(postId);
        return new RatingResponse(avg, count);

    }
}