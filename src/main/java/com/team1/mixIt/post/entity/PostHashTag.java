package com.team1.mixIt.post.entity;

import com.team1.mixIt.common.entity.BaseEntity;
import com.team1.mixIt.post.entity.Post;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_hashtage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostHashTag extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, length = 50)
    private String hastagl;

}
