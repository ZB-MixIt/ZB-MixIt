package com.team1.mixIt.tag.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.tag.dto.response.AutoCompleteResponse;
import com.team1.mixIt.tag.service.TagAutoCompleteService;
import com.team1.mixIt.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/tags")
@Tag(name = "태그 자동완성", description = "입력 기반 인기 태그 자동완성")
@RequiredArgsConstructor
public class TagAutoCompleteController {
    private final TagAutoCompleteService tagAutoCompleteService;

    @Operation(summary = "태그 자동완성", description = "prefix로 시작하는 태그를 추천 순으로 반환")
    @GetMapping("/autocomplete")
    public ResponseTemplate<List<AutoCompleteResponse>> autocomplete(
            @RequestParam("prefix") String prefix,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @AuthenticationPrincipal User user
    ) {
        List<AutoCompleteResponse> result = tagAutoCompleteService.autocomplete(prefix, limit, user);
        return ResponseTemplate.ok(result);
    }
}
