package com.team1.mixIt.post.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.post.dto.request.PostCreateRequest;
import com.team1.mixIt.post.dto.request.PostUpdateRequest;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.dto.response.LikeResponse;
import com.team1.mixIt.post.exception.BadRequestException;
import com.team1.mixIt.post.service.PostLikeService;
import com.team1.mixIt.post.service.PostService;
import com.team1.mixIt.image.entity.Image;
import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Operation(summary = "게시물 생성 (JSON only)", description = "이미지 없이 JSON body 로 생성합니다.")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseTemplate<Long> createPost(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PostCreateRequest dto
    ) {
        Long postId = postService.createPost(user.getId(), dto);
        return ResponseTemplate.ok(postId);
    }

    @Operation(summary = "게시물 생성 (multipart)", description = "이미지 포함/미포함 모두 지원합니다.")
    @Parameter(name = "dto", description = "게시물 텍스트 및 imageIds(JSON)", required = true)
    @Parameter(name = "newImages", description = "업로드할 이미지 파일들", required = false)
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseTemplate<Long> createPostMultipart(
            @AuthenticationPrincipal User user,
            @Valid @RequestPart("dto") PostCreateRequest dto,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages
    ) {
        List<Long> imageIds = validateAndUploadImages(user, newImages);
        dto.setImageIds(imageIds);
        Long postId = postService.createPost(user.getId(), dto);
        return ResponseTemplate.ok(postId);
    }

    @Operation(summary = "게시물 상세 조회", description = "게시물을 조회하고 조회수를 증가시킵니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{id}")
    public ResponseTemplate<PostResponse> getPostById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        Long currentUserId = user != null ? user.getId() : null;
        PostResponse dto = postService.getPostById(id, currentUserId, imageService);
        return ResponseTemplate.ok(dto);
    }

    @Operation(
            summary = "게시물 수정",
            description = "JSON만 수정하거나, 이미지 추가/삭제 + 텍스트·태그 수정 모두 지원합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "수정 성공",
            content = @Content(schema = @Schema(implementation = PostResponse.class))
    )
    @PutMapping(
            value = "/{id}",
            consumes = {
                    MediaType.APPLICATION_JSON_VALUE,
                    MediaType.MULTIPART_FORM_DATA_VALUE
            },
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseTemplate<PostResponse> updatePost(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            // application/json 요청일 땐 body 전체가 이 dto로 바인딩
            // multipart/form-data 요청일 땐 dto 파트(json)로 바인딩
            @RequestPart("dto") @Valid PostUpdateRequest dto,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,
            @RequestPart(value = "removeImageIds", required = false) List<Long> removeImageIds
    ) {
        // 신규 업로드
        List<Long> uploadedIds = validateAndUploadImages(user, newImages);

        // 기존에 남길 IDs 계산
        List<Long> original = dto.getImageIds() != null
                ? dto.getImageIds()
                : Collections.emptyList();
        List<Long> retained = original.stream()
                .filter(id0 -> removeImageIds == null || !removeImageIds.contains(id0))
                .toList();

        // 최종 imageIds = retained + uploaded
        List<Long> finalImageIds = new ArrayList<>(retained);
        finalImageIds.addAll(uploadedIds);
        dto.setImageIds(finalImageIds);

        // 업데이트 로직 실행
        postService.updatePost(user.getId(), id, dto);

        //결과 조회 후 반환
        PostResponse resp = postService.getPostById(id, user.getId(), imageService);
        return ResponseTemplate.ok(resp);
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
            @PathVariable Long postId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseTemplate.ok(likeService.status(postId, user.getId()));
    }

    //  공통 이미지 검증 & 업로드 로직
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
