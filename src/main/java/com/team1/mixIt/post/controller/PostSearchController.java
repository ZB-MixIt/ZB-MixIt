package com.team1.mixIt.post.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.post.dto.request.PostSearchRequest;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.service.PostSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts/search")
@RequiredArgsConstructor
@Tag(name = "게시물 검색", description = "게시물 검색 API")
public class PostSearchController {

    private final PostSearchService searchService;
    @Operation(
            summary = "게시물 검색",
            description = "주어진 검색 조건에 따라 게시물을 검색합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "게시물 검색 성공")
            }
    )
    @GetMapping
    public ResponseTemplate<Page<PostResponse>> search(PostSearchRequest req) {
        return ResponseTemplate.ok(searchService.search(req));
    }
}
