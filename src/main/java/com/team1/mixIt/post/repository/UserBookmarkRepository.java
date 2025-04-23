package com.team1.mixIt.post.repository;

import com.team1.mixIt.post.entity.UserBookmark;
import com.team1.mixIt.post.entity.UserBookmarkId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBookmarkRepository extends JpaRepository<UserBookmark, UserBookmarkId> {

    @EntityGraph(attributePaths = "post")
    Page<UserBookmark> findAllByIdUserId(Long userId, Pageable pageable);

}
