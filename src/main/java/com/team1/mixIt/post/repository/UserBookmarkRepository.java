package com.team1.mixIt.post.repository;

import com.team1.mixIt.post.entity.UserBookmark;
import com.team1.mixIt.post.entity.UserBookmarkId;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBookmarkRepository extends JpaRepository<UserBookmark, UserBookmarkId> {
    boolean existsByIdUserIdAndIdPostId(Long userId, Long postId);

    @Query("""
        SELECT ub 
        FROM UserBookmark ub
        JOIN FETCH ub.post p
        JOIN FETCH p.user               
        WHERE ub.id.userId = :userId
    """)
    Page<UserBookmark> findAllByIdUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );
}