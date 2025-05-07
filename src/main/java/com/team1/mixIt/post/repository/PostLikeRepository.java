package com.team1.mixIt.post.repository;

import com.team1.mixIt.post.entity.PostLike;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);
    long countByPostId(Long postId);

    @Modifying
    @Query("DELETE FROM PostLike pl WHERE pl.postId = :postId AND pl.userId = :userId")
    void deleteByPostIdAndUserId(@Param("postId") Long postId,
                                 @Param("userId") Long userId);
}
