package com.team1.mixIt.post.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.post.dto.request.PostCreateRequest;
import com.team1.mixIt.post.dto.request.PostUpdateRequest;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.dto.response.LikeResponse;
import com.team1.mixIt.post.enums.Category;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping(value = "/api/v1/posts", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "게시판 API", description = "게시물 작성·조회·수정·삭제 및 검색·페이징 API")
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
    public ResponseTemplate<Long> createPost(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PostCreateRequest dto
    ) {
        Long postId = postService.createPost(user.getId(), dto);
        return ResponseTemplate.ok(postId);
    }

    @Operation(summary = "전체 게시물 목록 조회", description = "카테고리, 키워드, 정렬, 페이징 조건으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseTemplate<List<PostResponse>> getAllPosts(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<PostResponse> list = postService.getAllPosts(
                user.getId(), category, keyword,
                sortBy, sortDir, page, size
        );
        return ResponseTemplate.ok(list);
    }

    @Operation(summary = "게시물 상세 조회", description = "게시물을 조회하고 조회수를 증가시킵니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{id}")
    public ResponseTemplate<PostResponse> getPostById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseTemplate.ok(postService.getPostById(id, user.getId()));
    }

    @Operation(summary = "게시물 수정", description = "내가 쓴 게시물을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "검증 실패")
    })
    @PutMapping("/{id}")
    public ResponseTemplate<Void> updatePost(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody PostUpdateRequest dto
    ) {
        postService.updatePost(user.getId(), id, dto);
        return ResponseTemplate.ok();
    }

    @Operation(summary = "게시물 삭제", description = "내가 쓴 게시물을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @DeleteMapping("/{id}")
    public ResponseTemplate<Void> deletePost(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        postService.deletePost(user.getId(), id);
        return ResponseTemplate.ok();
    }

    @Operation(summary = "게시물 좋아요 등록", description = "게시물에 좋아요를 남깁니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "좋아요 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseTemplate<LikeResponse> createLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseTemplate.ok(likeService.addLike(postId, user.getId()));
    }

    @Operation(summary = "게시물 좋아요 해제", description = "좋아요를 취소합니다.")
    @ApiResponse(responseCode = "204", description = "좋아요 해제 성공")
    @DeleteMapping("/{postId}/like")
    public ResponseTemplate<Void> deleteLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal User user
    ) {
        likeService.removeLike(postId, user.getId());
        return ResponseTemplate.ok();
    }

    @Operation(summary = "좋아요 상태 조회", description = "사용자의 좋아요 여부와 좋아요 수를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{postId}/like")
    public ResponseTemplate<LikeResponse> getLikeStatus(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId
    ) {
        return ResponseTemplate.ok(likeService.status(postId, user.getId()));
    }
}
