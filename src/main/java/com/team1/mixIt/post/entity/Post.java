package com.team1.mixIt.post.entity;

import com.team1.mixIt.common.config.ImageIdListConverter;
import com.team1.mixIt.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Convert(converter = ImageIdListConverter.class)
    @Column(name = "image_ids", columnDefinition = "JSON")
    @Builder.Default
    private List<Long> imageIds = new ArrayList<>();

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name= "like_count")
    private long likeCount;

    @Column(name = "bookmark_count")
    private Integer bookmarkCount = 0;

    @Column(name = "avg_rating")
    private double avgRating = 0.0;


}