package com.team1.mixIt.post.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.post.dto.response.HomeFeedResponse;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.service.HomeFeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        value = "/api/v1/home",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
@Tag(name = "홈 피드", description = "메인 홈화면 전용 API")
public class HomeFeedController {

    private final HomeFeedService feedService;

    @Operation(
            summary = "홈: 카테고리별 최신 게시물",
            description = "카페·음식점·편의점·기타 각 탭용, 최근 24시간 내 등록된 최신 게시물을 페이징하여 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ResponseTemplate.class))
    )
    @GetMapping("/category/{category}")
    public ResponseTemplate<Page<PostResponse>> category(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseTemplate.ok(
                feedService.getHomeByCategory(category, page, size)
        );
    }

    @Operation(summary = "홈: 오늘의 인기 조회수 Top5",
            description = "당일(00:00~24:00) 조회수 순으로 상위 5개 게시물을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ResponseTemplate.class))
    )
    @GetMapping("/views")
    public ResponseTemplate<Page<PostResponse>> views() {
        return ResponseTemplate.ok(
                feedService.getTodayTopViewed(0, 5)
        );
    }

    @Operation(summary = "홈: 주간 인기 조회수 Top5",
            description = "최근 7일(월~일) 조회수 순으로 상위 5개 게시물을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ResponseTemplate.class))
    )
    @GetMapping("/views/weekly")
    public ResponseTemplate<Page<PostResponse>> weeklyViews() {
        return ResponseTemplate.ok(
                feedService.getWeeklyTopViewed(0, 5)
        );
    }

    @Operation(summary = "홈: 인기 조합 더보기",
            description = "당일 조회수 기준 게시물 목록을 페이징하여 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ResponseTemplate.class))
    )
    @GetMapping("/popular/combos")
    public ResponseTemplate<Page<PostResponse>> popularCombos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseTemplate.ok(
                feedService.getTodayTopViewed(page, size)
        );
    }

    @Operation(summary = "홈: 오늘의 추천 북마크 Top4",
            description = "당일 북마크 순으로 상위 4개 게시물을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ResponseTemplate.class))
    )
    @GetMapping("/bookmarks")
    public ResponseTemplate<Page<PostResponse>> bookmarks() {
        return ResponseTemplate.ok(
                feedService.getTodayTopBookmarked(0, 4)
        );
    }

    @Operation(summary = "홈: 추천 게시물 더보기",
            description = "당일 북마크 기준 게시물 목록을 페이징하여 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ResponseTemplate.class))
    )
    @GetMapping("/recommendations/today")
    public ResponseTemplate<HomeFeedResponse> recommendedToday(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseTemplate.ok(
                feedService.getTodayRecommendations(page, size)
        );
    }

}
