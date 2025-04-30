package com.team1.mixIt.image.repository;

import com.team1.mixIt.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByUserIsNullAndCreatedAtBefore(LocalDateTime cutoff);

    // 게시물에 매핑된 이미지 조회
    List<Image> findByReviewId(Long reviewId);
    // 리뷰에 매핑된 이미지 조회
    List<Image> findByReviewPostId(Long postId);
}
