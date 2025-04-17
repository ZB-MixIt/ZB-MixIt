package com.team1.mixIt.post.entity;

import com.team1.mixIt.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;


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

    @Column(name = "image_ids", columnDefinition = "TEXT")
    private String imageIds;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name= "like_count")
    private Integer likeCount;

    @Column(name = "bookmark_count")
    private Integer bookmarkCount;

    @Column(name = "avg_rating")
    private double avgRating;

    @PrePersist
    private void init() {
        if(viewCount == null) viewCount = 0;
        if(likeCount == null) likeCount = 0;
        if(bookmarkCount == null) bookmarkCount = 0;
        avgRating = 0.0;
    }
}