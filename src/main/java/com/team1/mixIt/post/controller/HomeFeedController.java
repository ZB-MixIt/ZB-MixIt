package com.team1.mixIt.post.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.tag.dto.response.TagStatResponse;
import com.team1.mixIt.post.service.HomeFeedService;
import com.team1.mixIt.tag.service.TagStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/home", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "홈 피드", description = "메인 홈화면 전용 API")
public class HomeFeedController {

    private final HomeFeedService feedService;
    private final TagStatsService tagStatsService;

    @Operation(
            summary = "홈: 카테고리별 최신 5개",
            description = "카페·음식점·편의점·기타 각 탭용 최신 게시물 5개를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ResponseTemplate.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 카테고리"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/category/{category}")
    public ResponseTemplate<List<PostResponse>> category(
            @Parameter(description = "카테고리 이름", example = "카페")
            @PathVariable String category
    ) {
        return ResponseTemplate.ok(
                feedService.getHomeByCategory(category, 5)
        );
    }

    @Operation(
            summary = "홈: 오늘의 인기 조회수 Top5",
            description = "당일 조회수 순으로 상위 5개 게시물을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ResponseTemplate.class))
            ),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/views")
    public ResponseTemplate<List<PostResponse>> views() {
        return ResponseTemplate.ok(
                feedService.getTodayTopViewed(0, 5).getContent()
        );
    }

    @Operation(
            summary = "홈: 오늘의 추천 북마크 Top4",
            description = "당일 북마크 순으로 상위 4개 게시물을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ResponseTemplate.class))
            ),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/bookmarks")
    public ResponseTemplate<List<PostResponse>> bookmarks() {
        return ResponseTemplate.ok(
                feedService.getTodayTopBookmarked(0, 4).getContent()
        );
    }

    @Operation(
            summary = "홈: 오늘의 인기 태그 3개",
            description = "당일 태그 사용 빈도 순으로 상위 3개 태그를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ResponseTemplate.class))
            ),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/tags")
    public ResponseTemplate<List<TagStatResponse>> tags() {
        return ResponseTemplate.ok(
                tagStatsService.getTopTags(3)
        );
    }
}
