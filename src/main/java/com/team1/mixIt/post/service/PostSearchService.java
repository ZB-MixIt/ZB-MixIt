package com.team1.mixIt.post.service;

import com.team1.mixIt.post.dto.request.PostSearchRequest;
import com.team1.mixIt.post.dto.response.PostResponse;
import org.springframework.data.domain.Page;

public interface PostSearchService {
    Page<PostResponse> search(PostSearchRequest req);
}
