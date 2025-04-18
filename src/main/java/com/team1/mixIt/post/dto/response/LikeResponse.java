package com.team1.mixIt.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeResponse {
    private boolean hasLiked;
    private long likeCount;
}