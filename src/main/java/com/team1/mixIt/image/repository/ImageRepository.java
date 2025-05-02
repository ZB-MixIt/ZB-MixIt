package com.team1.mixIt.image.repository;

import com.team1.mixIt.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByUserIsNullAndCreatedAtBefore(LocalDateTime cutoff);

    List<Image> findAllByIdIn(List<Long> ids);
}
