package com.team1.mixIt.post.repository;

import com.team1.mixIt.post.entity.PostRating;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface PostRatingRepository extends JpaRepository<PostRating, Long> {
    Optional<PostRating> findByPostIdAndUserId(Long postId, Long userId);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    void deleteByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);

    @Query("SELECT AVG(r.rate) FROM PostRating r WHERE r.postId = :postId AND r.rate > 0")
    BigDecimal findAverageRateByPostId(@Param("postId") Long postId);

}
