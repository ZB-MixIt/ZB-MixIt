package com.team1.mixIt.tag.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.tag.dto.response.TagStatResponse;
import com.team1.mixIt.tag.entity.TagStats;
import com.team1.mixIt.tag.repository.TagStatsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(
        value = "/api/v1/tags/popular",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
@Tag(name = "인기 태그", description = "태그 사용 빈도 기반 TOP API")
public class TagStatsController {
    private final TagStatsRepository statsRepo;

    @Operation(
            summary = "인기 태그 TOP 조회",
            description = "useCount 기준 상위 태그를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping
    public ResponseTemplate<List<TagStatResponse>> topTags(
            @Parameter(description = "가져올 태그 개수", example = "10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        var page = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "useCount"));
        List<TagStats> stats = statsRepo.findAll(page).getContent();

        List<TagStatResponse> dto = stats.stream()
                .map(ts -> new TagStatResponse(ts.getTag(), ts.getUseCount()))
                .collect(Collectors.toList());

        return ResponseTemplate.ok(dto);
    }
}
