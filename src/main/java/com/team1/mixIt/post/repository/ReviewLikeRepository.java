package com.team1.mixIt.post.repository;

import com.team1.mixIt.post.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);

    void deleteByReviewIdAndUserId(Long reviewId, Long userId);
}
