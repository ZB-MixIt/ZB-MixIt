package com.team1.mixIt.post.dto.response;

import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.post.repository.PostLikeRepository;
import com.team1.mixIt.post.repository.PostRatingRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "내 북마크 게시물 응답 DTO")
public class BookmarkResponse {

    @Schema(description = "게시물 고유 ID", example = "42")
    private final Long id;

    @Schema(description = "게시물 제목", example = "서브웨이 꿀조합")
    private final String title;

    @Schema(description = "작성자 프로필 이미지 URL", example = "https://.../profile.jpg")
    private final String authorProfileImage;

    @Schema(description = "별점 평균", example = "4.5")
    private final Double avgRating;

    @Schema(description = "하트(좋아요 여부)", example = "true")
    private final Boolean hasLiked;

    @Schema(description = "첨부 이미지 목록 (ID + URL)", implementation = BookmarkResponse.ImageDto.class)
    private final List<ImageDto> images;

    @Schema(description = "작성자 ID", example = "7")
    private final Long authorId;

    @Schema(description = "작성자 닉네임", example = "mixItUser")
    private final String authorNickname;

    @Schema(description = "북마크 수", example = "17")
    private final Integer bookmarkCount;


    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Schema(description = "이미지 DTO (ID + URL)")
    public static class ImageDto {
        @Schema(description = "이미지 고유 ID", example = "1")
        private final Long id;

        @Schema(description = "이미지 URL", example = "https://.../img1.jpg")
        private final String src;
    }


    public static BookmarkResponse fromEntity(
            com.team1.mixIt.post.entity.Post post,
            Long currentUserId,
            ImageService imageService,
            PostLikeRepository postLikeRepository,
            PostRatingRepository postRatingRepository,
            String defaultImageUrl
    ) {
        Long postId = post.getId();
        String title = post.getTitle();

        String authorProfileImage = null;
        com.team1.mixIt.user.entity.User author = post.getUser();
        if (author.getProfileImage() != null) {
            authorProfileImage = author.getProfileImage().getUrl();
        }
        String authorNickname = author.getNickname();
        Long authorId = author.getId();

        java.math.BigDecimal avgBd = postRatingRepository.findAverageRateByPostId(postId);
        Double avgRating = (avgBd != null) ? avgBd.doubleValue() : 0.0;

        Boolean hasLiked = false;
        if (currentUserId != null) {
            hasLiked = postLikeRepository
                    .findByPostIdAndUserId(postId, currentUserId)
                    .isPresent();
        }

        List<ImageDto> imageDtos = post.getImageIds().stream()
                .map(imgId -> {
                    String url = imageService.findById(imgId).getUrl();
                    return ImageDto.builder()
                            .id(imgId)
                            .src(url)
                            .build();
                })
                .toList();

        Integer bookmarkCount = post.getBookmarkCount();

        return BookmarkResponse.builder()
                .id(postId)
                .title(title)
                .authorProfileImage(authorProfileImage)
                .avgRating(avgRating)
                .hasLiked(hasLiked)
                .images(imageDtos)
                .authorId(authorId)
                .authorNickname(authorNickname)
                .bookmarkCount(bookmarkCount)
                .build();
    }
}
