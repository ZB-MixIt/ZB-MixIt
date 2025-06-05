package com.team1.mixIt.post.repository;

import com.team1.mixIt.post.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);

    void deleteByReviewIdAndUserId(Long reviewId, Long userId);

    long countByReviewId(Long reviewId);

    List<ReviewLike> findAllByUserIdAndReviewIdIn(Long userId, List<Long> reviewIds);

    // 리뷰 좋아요는 review_id로 삭제
    void deleteByReviewId(Long reviewId);
    // 만약 한 번에 여러 리뷰 ID를 한꺼번에 삭제하고 싶으면
    void deleteByReviewIdIn(List<Long> reviewIds);
}
