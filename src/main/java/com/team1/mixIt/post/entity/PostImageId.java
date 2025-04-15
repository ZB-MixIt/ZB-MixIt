package com.team1.mixIt.post.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostImageId implements Serializable {
    private Long postId;
    private Long imageId;
}

