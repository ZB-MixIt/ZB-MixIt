package com.team1.mixIt.post.dto.response;

import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.entity.PostHashtag;
import com.team1.mixIt.post.enums.Category;
import com.team1.mixIt.post.service.PostBookmarkService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@Schema(description = "게시판 응답 DTO")
public class PostResponse {

    @Schema(description = "게시물 고유 ID", example = "1")
    private Long id;

    @Schema(description = "작성자 ID", example = "1")
    private Long userId;

    @Schema(description = "작성자 닉네임", example = "gleetest")
    private String authorNickname;

    @Schema(description = "작성자 프로필 이미지 URL", example = "https://s3.bucket/profile.jpg")
    private String authorProfileImage;

    @Schema(description = "카테고리", example = "CAFE")
    private Category category;

    @Schema(description = "게시물 제목", example = "게시물 제목")
    private String title;

    @Schema(description = "게시물 내용", example = "게시물 내용")
    private String content;

    @Schema(description = "첨부 이미지 목록", implementation = PostResponse.ImageDto.class)
    private List<ImageDto> images;

    @Schema(description = "대표 이미지 URL (없으면 기본 이미지)", example = "https://../기본이미지.png")
    private String defaultImage;

    @Schema(description = "조회수", example = "0")
    private Integer viewCount;

    @Schema(description = "현재 사용자가 이 게시물을 좋아요한 상태", example = "true")
    private Boolean hasLiked;

    @Schema(description = "좋아요 수", example = "0")
    private Long likeCount;

    @Schema(description = "현재 사용자가 이 게시물을 북마크한 상태인지", example = "true")
    private Boolean hasBookmarked;

    @Schema(description = "북마크 수", example = "0")
    private Integer bookmarkCount;

    @Schema(description = "게시물 태그 목록")
    private List<String> tags;

    @Schema(description = "현재 사용자가 작성자인지 여부", example = "true")
    private Boolean isAuthor;

    @Schema(description = "별점 정보")
    private RatingResponse rating;

    @Schema(description = "게시물 생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "게시물 수정일시")
    private LocalDateTime updatedAt;


    @Getter
    @Setter
    @Schema(description = "이미지 DTO")
    public static class ImageDto {
        @Schema(description = "이미지 고유 ID", example = "1")
        private Long id;

        @Schema(description = "이미지 URL", example = "https://.../img1.jpg")
        private String src;

        public ImageDto(Long id, String src) {
            this.id = id;
            this.src = src;
        }
    }

    @Getter
    @Setter
    @Schema(description = "별점 응답 DTO")
    public static class RatingResponse {
        @Schema(description = "평균 별점", example = "4.5")
        private Double averageRating;

        @Schema(description = "별점 투표 수", example = "10")
        private Long ratingCount;

        public RatingResponse(Double averageRating, Long ratingCount) {
            this.averageRating = averageRating;
            this.ratingCount = ratingCount;
        }
    }

    /**
     * Entity → DTO 변환 메서드
     * 마지막에 하드코딩된 좋아요 개수(0L)나 hasLiked(false)를 제거하고,
     * 호출부에서 넘겨준 likeCount, hasLiked 값을 그대로 사용합니다.
     */
    public static PostResponse fromEntity(
            Post p,
            Long currentUserId,
            String defaultImageUrl,
            ImageService imageService,
            PostBookmarkService bookmarkService,
            RatingResponse rating,
            Long likeCount,
            Boolean hasLiked
    ) {
        // 1) 첨부 이미지 목록 생성
        List<ImageDto> imgDtos = p.getImageIds().stream()
                .map(imageService::findById)
                .map(img -> new ImageDto(img.getId(), img.getUrl()))
                .toList();

        // 2) 작성자 닉네임
        String nickname = p.getUser().getNickname();

        // 3) 북마크 여부
        boolean bookmarkedFlag = bookmarkService.isBookmarked(p.getId(), currentUserId);

        // 4) 작성자 프로필 이미지 URL
        String profileUrl = null;
        if (p.getUser().getProfileImageId() != null) {
            profileUrl = imageService.findById(p.getUser().getProfileImageId()).getUrl();
        }

        // 5) 작성자 본인 여부
        boolean authorFlag = (currentUserId != null && p.getUserId().equals(currentUserId));

        // 6) 빌더로 최종 응답 객체 생성
        return PostResponse.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .authorNickname(nickname)
                .authorProfileImage(profileUrl)
                .category(p.getCategory())
                .title(p.getTitle())
                .content(p.getContent())
                .images(imgDtos)
                .defaultImage(defaultImageUrl)
                .viewCount(p.getViewCount())
                .hasLiked(hasLiked)
                .likeCount(likeCount)
                .hasBookmarked(bookmarkedFlag)
                .bookmarkCount(p.getBookmarkCount())
                .tags(p.getHashtag().stream().map(PostHashtag::getHashtag).toList())
                .isAuthor(authorFlag)
                .rating(rating)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getModifiedAt())
                .build();
    }
}
