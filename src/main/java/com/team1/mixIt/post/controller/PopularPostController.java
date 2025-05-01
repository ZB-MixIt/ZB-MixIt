package com.team1.mixIt.post.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.service.PopularPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/posts/popular", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "인기 게시물", description = "메인화면 인기 조합 · 오늘의 추천 · 좋아요 Top API")
public class PopularPostController {
    private final PopularPostService popularPostService;

    @Operation(summary = "오늘의 인기 조회수 Top5", description = "당일 조회수 기준 상위 게시물 5개를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })

    @GetMapping("/views")
    public ResponseTemplate<List<PostResponse>> topViews(
            @Parameter(description = "가져올 게시물 개수", example = "5")
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseTemplate.ok(popularPostService.getTodayTopViewed(limit));
    }

    @Operation(summary = "오늘의 인기 북마크 Top4", description = "당일 북마크 수 기준 상위 게시물 4개를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/bookmarks")
    public ResponseTemplate<List<PostResponse>> topBookmarks(
            @Parameter(description = "가져올 게시물 개수", example = "4")
            @RequestParam(defaultValue = "4") int limit
    ) {
        return ResponseTemplate.ok(popularPostService.getTodayTopBookmarked(limit));
    }

    @Operation(summary = "오늘의 인기 좋아요 Top5", description = "당일 좋아요 수 기준 상위 게시물 5개를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/likes")
    public ResponseTemplate<List<PostResponse>> topLikes(
            @Parameter(description = "가져올 게시물 개수", example = "5")
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseTemplate.ok(popularPostService.getTodayTopLiked(limit));
    }
}
