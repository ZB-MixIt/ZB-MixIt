package com.team1.mixIt.post.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review_like",
        uniqueConstraints = @UniqueConstraint(columnNames = {"review_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public ReviewLike(Long reviewId, Long userId) {
        this.reviewId = reviewId;
        this.userId = userId;
    }
}
