package com.team1.mixIt.image.entity;

import com.team1.mixIt.common.entity.BaseEntity;
import com.team1.mixIt.post.entity.Review;
import com.team1.mixIt.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Image extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String url;

    public void updateUser(User user) {
        this.user = user;
    }
}