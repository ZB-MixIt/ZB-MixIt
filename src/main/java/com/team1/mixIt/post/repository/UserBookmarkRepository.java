package com.team1.mixIt.post.repository;

import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.entity.UserBookmark;
import com.team1.mixIt.post.entity.UserBookmarkId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBookmarkRepository extends JpaRepository<UserBookmark, UserBookmarkId> {
    Page<UserBookmark> findAllByIdUserId(Long userId, Pageable pageable);
}