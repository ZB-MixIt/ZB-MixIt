package com.team1.mixIt.post.repository;

import com.team1.mixIt.post.entity.Review;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {
    @EntityGraph(attributePaths = {"user", "user.profileImage"})
    List<Review> findByPostIdOrderByCreatedAtDesc(Long postId);

    Optional<Review> findByIdAndUserId(Long id, Long userId);

    long countByPostId(Long postId);

    boolean existsByIdAndPostId(Long id, Long postId);

    @Modifying
    @Query("UPDATE Review r SET r.likeCount = r.likeCount + 1 WHERE r.id = :reviewId")
    void increaseLikeCount(@Param("reviewId") Long reviewId);

    @Modifying
    @Query("UPDATE Review r SET r.likeCount = r.likeCount - 1 WHERE r.id = :reviewId AND r.likeCount > 0")
    void decreaseLikeCount(@Param("reviewId") Long  reviewId);

    @Override
    @EntityGraph(value = "Review.withPost", type = EntityGraph.EntityGraphType.LOAD)
    @NotNull Page<Review> findAll(@NotNull Specification<Review> spec, @NotNull Pageable pageable);
}
