package com.team1.mixIt.post.repository;

import com.team1.mixIt.post.entity.Review;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByPostIdOrderByRateDescCreatedAtDesc(Long postId);
    Optional<Review> findByIdAndUserId(Long id, Long userId);

    long countByPostId(Long postId);

    boolean existsByIdAndPostId(Long id, Long postId);

@Query("SELECT COALESCE(AVG(r.rate), 0) FROM Review r WHERE r.post.id = :postId")
    BigDecimal findAverageRateByPostId(@Param("postId") Long postId);

}
