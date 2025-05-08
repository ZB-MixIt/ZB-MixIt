package com.team1.mixIt.post.repository;

import com.team1.mixIt.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>,
                                        JpaSpecificationExecutor<Post> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    void increaseLikeCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :postId")
    void decreaseLikeCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.bookmarkCount = p.bookmarkCount + 1 WHERE p.id = :postId")
    void increaseBookmarkCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.bookmarkCount = p.bookmarkCount - 1 WHERE p.id = :postId")
    void decreaseBookmarkCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void increaseViewCount(@Param("postId") Long postId);

    @Override
    @EntityGraph(value = "Post.withHashtags", type = EntityGraph.EntityGraphType.LOAD)
    Page<Post> findAll(Specification<Post> spec, Pageable pageable);

    @Override
    @EntityGraph(value = "Post.withHashtags", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Post> findById(Long id);

    @Query("SELECT p FROM Post p JOIN FETCH p.user u LEFT JOIN FETCH u.profileImage WHERE p.id = :id")
    Optional<Post> findWithUserAndProfileImageById(@Param("id") Long id);


    @Query("""
    SELECT DISTINCT p
      FROM Post p
      JOIN FETCH p.user u
      LEFT JOIN FETCH u.profileImage img
      LEFT JOIN FETCH p.hashtag h
     WHERE p.id = :id
    """)
    Optional<Post> findWithAllById(@Param("id") Long id);
}
