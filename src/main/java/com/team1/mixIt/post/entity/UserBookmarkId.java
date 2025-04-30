package com.team1.mixIt.post.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserBookmarkId  implements Serializable {
    private Long userId;
    private Long postId;
}
