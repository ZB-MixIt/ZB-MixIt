package com.team1.mixIt.image.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.image.entity.Image;
import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Tag(name = "Image")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/images")
public class ImageController {

    private final ImageService imageService;

    @PostMapping
    @Operation(
        summary = "Upload Image",
        description = "이미지 업로드 API"
    )
    public ResponseTemplate<UploadImageResponse> uploadImage(@AuthenticationPrincipal User user,
                                                             @RequestPart(value = "image") MultipartFile file) {
        Image image;
        if (Objects.isNull(user)) {
            image = imageService.create(file);
        } else {
            image = imageService.create(file, user.getLoginId());
        }
        return ResponseTemplate.ok(UploadImageResponse.of(image));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete Image",
            description = "이미지 삭제 API"
    )
    public ResponseTemplate<Void> deleteImage(@AuthenticationPrincipal User user,
                                              @PathVariable Long id) {
        imageService.delete(id, user);
        return ResponseTemplate.ok();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UploadImageResponse {
        private Long id;
        private String url;

        public static UploadImageResponse of(Image image) {
            return UploadImageResponse.builder()
                    .id(image.getId())
                    .url(image.getUrl())
                    .build();
        }
    }
}
