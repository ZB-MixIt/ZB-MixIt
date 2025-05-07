package com.team1.mixIt.post.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.post.dto.request.PostCreateRequest;
import com.team1.mixIt.post.dto.request.PostUpdateRequest;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.dto.response.LikeResponse;
import com.team1.mixIt.post.enums.Category;
import com.team1.mixIt.post.exception.BadRequestException;
import com.team1.mixIt.post.service.PostLikeService;
import com.team1.mixIt.post.service.PostService;
import com.team1.mixIt.image.entity.Image;
import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Validated
@RestController
@RequestMapping(value = "/api/v1/posts", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "게시판 API", description = "게시물 작성/조회/수정/삭제 및 검색페이징 API")
public class PostController {

    private final PostService postService;
    private final PostLikeService likeService;
    private final ImageService imageService;

    // JSON-only 요청 (이미지 없는 경우)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "게시물 생성 (JSON only)", description = "이미지 없이 JSON body로 게시물을 생성합니다.")
    public ResponseTemplate<Long> createPostJson(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PostCreateRequest dto
    ) {
        dto.setImageIds(Collections.emptyList());
        return ResponseTemplate.ok(
                postService.createPost(user.getId(), dto)
        );
    }

    // multipart/form-data 요청 (이미지 포함 또는 없는 경우)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "게시물 생성 (multipart)", description = "이미지 포함/미포함 모두 지원하는 multipart/form-data 요청입니다.")
    public ResponseTemplate<Long> createPostMultipart(
            @AuthenticationPrincipal User user,
            @Valid @RequestPart("dto") PostCreateRequest dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        List<Long> imageIds = validateAndUploadImages(user, images);
        dto.setImageIds(imageIds);
        return ResponseTemplate.ok(
                postService.createPost(user.getId(), dto)
        );
    }

    @Operation(summary = "전체 게시물 목록 조회", description = "카테고리, 키워드, 정렬, 페이징 조건으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseTemplate<List<PostResponse>> getAllPosts(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseTemplate.ok(
                postService.getAllPosts(user.getId(), category, keyword, sortBy, sortDir, page, size)
        );
    }

    @Operation(summary = "게시물 상세 조회", description = "게시물을 조회하고 조회수를 증가시킵니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{id}")
    public ResponseTemplate<PostResponse> getPostById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        PostResponse dto = postService.getPostById(id, user.getId());
        dto.setIsAuthor(dto.getUserId().equals(user.getId()));
        return ResponseTemplate.ok(dto);
    }

    // 게시물 수정 (multipart/form-data 전용)
    @Operation(summary = "게시물 수정", description = "이미지 포함/미포함 수정 지원")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseTemplate<Void> updatePost(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestPart("dto") PostUpdateRequest dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        List<Long> imageIds = validateAndUploadImages(user, images);
        dto.setImageIds(imageIds);
        postService.updatePost(user.getId(), id, dto);
        return ResponseTemplate.ok();
    }

    // 게시물 삭제
    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    public ResponseTemplate<Void> deletePost(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        postService.deletePost(user.getId(), id);
        return ResponseTemplate.ok();
    }

    // 좋아요 등록/해제
    @Operation(summary = "게시물 좋아요 등록", description = "게시물에 좋아요를 남깁니다.")
    @ApiResponse(responseCode = "201", description = "좋아요 등록 성공")
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

    // 공통 이미지 검증 & 업로드 로직
    private List<Long> validateAndUploadImages(User user, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return Collections.emptyList();
        }
        if (images.size() > 10) {
            throw new BadRequestException("최대 10장까지 업로드 가능합니다.");
        }

        List<Long> ids = new ArrayList<>();
        for (MultipartFile file : images) {
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new BadRequestException("이미지 파일은 10MB 이하만 가능합니다.");
            }
            String ext = Objects.requireNonNull(file.getOriginalFilename())
                    .substring(file.getOriginalFilename().lastIndexOf('.') + 1)
                    .toLowerCase();
            if (!List.of("jpg", "jpeg", "png").contains(ext)) {
                throw new BadRequestException("JPG/PNG만 지원합니다.");
            }
            Image img = (user != null && user.getLoginId() != null)
                    ? imageService.create(file, user.getLoginId())
                    : imageService.create(file);
            ids.add(img.getId());
        }
        return ids;
    }
}
