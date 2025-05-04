package com.team1.mixIt.post.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.post.dto.request.PostSearchRequest;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.service.PostSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts/search")
@RequiredArgsConstructor
public class PostSearchController {

    private final PostSearchService searchService;

    @GetMapping
    public ResponseTemplate<Page<PostResponse>> search(PostSearchRequest req) {
        return ResponseTemplate.ok(searchService.search(req));
    }
}
