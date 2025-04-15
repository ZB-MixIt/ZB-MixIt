package com.team1.mixIt.post.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.post.dto.request.PostCreateRequest;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.dto.request.PostUpdateRequest;
import com.team1.mixIt.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "게시판 API", description = "게시판 관련 API 엔드포인트")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // 게시판 등록
    @Operation(summary = "게시판 등록", description = "새로운 게시물을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "입력 값 검증 실패")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseTemplate<Void> createPost(@Valid @RequestBody PostCreateRequest request) {
        postService.createPost(request);
        return ResponseTemplate.ok();
    }

    // 게시판 목록 조회
    @Operation(summary = "게시판 목록 조회", description = "모든 게시물 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseTemplate<List<PostResponse>> getAllPosts() {
        List<PostResponse> responses = postService.getAllPosts();
        return ResponseTemplate.ok(responses);
    }

    // 게시판 상세 조회
    @Operation(summary = "게시판 상세 조회", description = "특정 게시물의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseTemplate<PostResponse> getPostById(@PathVariable Long id) {
        PostResponse response = postService.getPostById(id);
        return ResponseTemplate.ok(response);
    }

    // 게시판 수정
    @Operation(summary = "게시판 수정", description = "특정 게시물의 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseTemplate<Void> updatePost(@PathVariable Long id,
                                             @Valid @RequestBody PostUpdateRequest request) {
        postService.updatePost(id, request);
        return ResponseTemplate.ok();
    }

    // 게시판 삭제
    @Operation(summary = "게시판 삭제", description = "특정 게시물을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseTemplate<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseTemplate.ok();
    }
}
