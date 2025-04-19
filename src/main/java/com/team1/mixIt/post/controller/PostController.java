package com.team1.mixIt.post.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.post.dto.request.PostCreateRequest;
import com.team1.mixIt.post.dto.request.PostUpdateRequest;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.dto.response.LikeResponse;
import com.team1.mixIt.post.service.PostLikeService;
import com.team1.mixIt.post.service.PostService;
import com.team1.mixIt.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "게시판 API", description = "게시판 관련 API")
public class PostController {

    private final PostService postService;
    private final PostLikeService likeService;


    @Operation(summary = "게시물 등록", description = "새로운 게시물을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "검증 실패")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseTemplate<Void> createPost(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PostCreateRequest dto
    ) {
        postService.createPost(user.getId(), dto);
        return ResponseTemplate.ok();
    }

    @Operation(summary = "게시물 목록 조회", description = "모든 게시물을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseTemplate<List<PostResponse>> getAllPosts() {
        return ResponseTemplate.ok(postService.getAllPosts());
    }

    @Operation(summary = "게시물 상세 조회", description = "특정 게시물을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseTemplate<PostResponse> getPostById(
            @PathVariable Long id
    ) {
        return ResponseTemplate.ok(postService.getPostById(id));
    }

    @Operation(summary = "게시물 수정", description = "특정 게시물을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "검증 실패")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseTemplate<Void> updatePost(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody PostUpdateRequest dto
    ) {
        postService.updatePost(user.getId(), id, dto);
        return ResponseTemplate.ok();
    }

    @Operation(summary = "게시물 삭제", description = "특정 게시물을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @DeleteMapping("/{id}")
    public ResponseTemplate<Void> deletePost(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        postService.deletePost(user.getId(), id);
        return ResponseTemplate.ok();
    }
}
