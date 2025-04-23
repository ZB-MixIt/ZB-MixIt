package com.team1.mixIt.post.entity;

import com.team1.mixIt.common.entity.BaseEntity;
import com.team1.mixIt.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
public class Review extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="post_id", nullable=false)
    private Post post;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal rate;

    @Builder
    public Review(User user, Post post, String content, BigDecimal rate) {
        this.user = user;
        this.post = post;
        this.content = content;
        this.rate = rate;
    }
}
