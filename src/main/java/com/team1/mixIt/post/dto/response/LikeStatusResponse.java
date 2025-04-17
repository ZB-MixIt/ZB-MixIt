package com.team1.mixIt.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeStatusResponse {
    private boolean liked;
    private long likeCount;
}
