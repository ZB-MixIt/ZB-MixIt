package com.team1.mixIt.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.image.entity.Image;
import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.post.dto.request.ReviewRequest;
import com.team1.mixIt.post.dto.response.ReviewResponse;
import com.team1.mixIt.post.exception.BadRequestException;
import com.team1.mixIt.post.service.PostService;
import com.team1.mixIt.post.service.ReviewService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/posts/{postId}/reviews")
@RequiredArgsConstructor
@Tag(name = "리뷰 API", description = "게시물 리뷰·평점 관리")
public class ReviewController {

    private final ReviewService svc;
    private final ImageService imageService;
    private final PostService postSvc;
    private final ObjectMapper objectMapper;

    @Operation(summary = "리뷰 등록 (JSON)", description = "게시물에 이미지 없이 리뷰와 평점을 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 등록 성공"),
            @ApiResponse(responseCode = "400", description = "검증 실패"),
            @ApiResponse(responseCode = "404", description = "게시물 없음"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @Validated
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseTemplate<ReviewResponse> createJson(
            @PathVariable Long postId,
            @AuthenticationPrincipal User user,
            @RequestBody @Valid ReviewRequest req
    ) {
        return ResponseTemplate.ok(svc.addReview(postId, user, req));
    }

    @Operation(summary = "리뷰 등록 (multipart)", description = "이미지 포함 리뷰 등록")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseTemplate<ReviewResponse> createMultipart(
            @PathVariable Long postId,
            @AuthenticationPrincipal User user,
            @RequestPart("dto") String dtoJson,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages
    ) throws IOException {
        ReviewRequest req = objectMapper.readValue(dtoJson, ReviewRequest.class);

        List<Long> imageIds = validateAndUploadImages(user, newImages);
        req.setImageIds(imageIds);

        return ResponseTemplate.ok(svc.addReview(postId, user, req));
    }


    @Operation(summary = "리뷰 수정 (JSON)", description = "텍스트만 수정")
    @PutMapping(
            path = "/{reviewId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseTemplate<ReviewResponse> updateJson(
            @PathVariable Long postId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user,
            @RequestBody @Valid ReviewRequest req
    ) {
        return ResponseTemplate.ok(svc.updateReview(reviewId, user, req));
    }

    @Operation(summary = "리뷰 수정 (multipart)", description = "이미지 변경/추가/삭제 + 텍스트 수정")
    @PutMapping(
            path = "/{reviewId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseTemplate<ReviewResponse> updateMultipart(
            @PathVariable Long postId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user,
            @RequestPart("dto") String dtoJson,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,
            @RequestPart(value = "removeImageIds", required = false) List<Long> removeImageIds
    ) throws IOException {
        ReviewRequest req = objectMapper.readValue(dtoJson, ReviewRequest.class);

        List<Long> original = req.getImageIds() != null ? req.getImageIds() : List.of();
        List<Long> retained = original.stream()
                .filter(id -> removeImageIds == null || !removeImageIds.contains(id))
                .toList();

        List<Long> uploaded = validateAndUploadImages(user, newImages);

        List<Long> finalIds = new ArrayList<>(retained);
        finalIds.addAll(uploaded);
        req.setImageIds(finalIds);

        return ResponseTemplate.ok(svc.updateReview(reviewId, user, req));
    }

    @Operation(summary = "리뷰 삭제", description = "본인이 작성한 리뷰를 삭제합니다.")
    @DeleteMapping("/{reviewId}")
    public ResponseTemplate<Void> delete(
            @PathVariable Long postId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user
    ) {
        svc.deleteReview(reviewId, user);
        return ResponseTemplate.ok();
    }

    @Operation(summary = "리뷰 목록", description = "게시물의 리뷰를 평점 순/최신순으로 조회합니다.")
    @GetMapping
    public ResponseTemplate<List<ReviewResponse>> list(
            @PathVariable Long postId,
            @AuthenticationPrincipal User user
    ) {
        if (!postSvc.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시물 없음");
        }
        Long currentUserId = (user != null) ? user.getId() : null;
        return ResponseTemplate.ok(svc.listReviews(postId, currentUserId));
    }

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