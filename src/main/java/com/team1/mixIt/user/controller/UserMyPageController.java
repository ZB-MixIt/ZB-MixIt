package com.team1.mixIt.user.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.image.entity.Image;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.entity.Review;
import com.team1.mixIt.post.service.PostService;
import com.team1.mixIt.post.service.ReviewService;
import com.team1.mixIt.user.entity.User;
import com.team1.mixIt.user.service.UserMyPageService;
import com.team1.mixIt.user.service.UserService;
import com.team1.mixIt.utils.DateUtils;
import com.team1.mixIt.utils.ImageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Tag(name = "User My Page")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/my-page")
public class UserMyPageController {

    private final UserService userService;
    private final PostService postService;
    private final ReviewService reviewService;
    private final UserMyPageService myPageService;

    @Operation(
            summary = "Get MyPage",
            description = "MyPage 조회 API"
    )
    @PostMapping()
    public ResponseTemplate<GetMyPageResponse> getMyPage(@AuthenticationPrincipal User user,
                                                         @Valid @RequestBody GetMyPageRequest request) {
        user = userService.getMyPage(user.getLoginId(), user.getPassword(), request.getPassword());
        return ResponseTemplate.ok(GetMyPageResponse.of(user));
    }

    @Operation(
            summary = "회원정보수정",
            description = "회원정보수정 API"
    )
    @PutMapping
    public ResponseTemplate<GetMyPageResponse> modifyMyPage(@AuthenticationPrincipal User user,
                                                            @Valid @RequestBody UpdateMyPageRequest request) {
        user = myPageService.updateMyPage(user, request);
        return ResponseTemplate.ok(GetMyPageResponse.of(user));
    }

    public record UpdateMyPageRequest (
            String nickname,
            Long imageId,

            @NotNull Boolean emailNotify,

            @NotNull Boolean smsNotify,
            @NotNull Boolean postLikeAlarm,
            @NotNull Boolean postReviewAlarm,
            @NotNull Boolean popularPostAlarm
    ) {}

    @Operation(
            summary = "Get Notification Information",
            description = "MyPage 하위 알림 정보 조회 API"
    )
    @GetMapping("/notification")
    public ResponseTemplate<GetMyPageNotificationResponse> getMyPageNotification(
            @AuthenticationPrincipal User user
    ) {
        return ResponseTemplate.ok(
                GetMyPageNotificationResponse.builder()
                        .eventNotification(user.isPostLikeAlarm())
                        .pushNotification(user.isPostLikeAlarm())
                        .build()
        );
    }

    @Operation(
            summary = "Update Notification Information",
            description = "MyPage 하위 알림 정보 수정 API"
    )
    @PostMapping("/notification")
    @Deprecated
    public ResponseTemplate<Void> updateNotification(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateNotificationRequest request
    ) {
        userService.updateNotificationSettings(
                user.getId(),
                request.getEvent(),
                request.getAlert()
        );
        return ResponseTemplate.ok();
    }

    @Operation(
            summary = "Get My Posts",
            description = "내 게시글 조회"
    )
    @GetMapping("/posts")
    public ResponseTemplate<Page<PostResponse>> getMyPagePosts(@AuthenticationPrincipal User user,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size,
                                                               @RequestParam(defaultValue = "latest") String sort) {
        Sort sortOption = switch (sort.toLowerCase()) {
            case "popular" -> Sort.by(Sort.Direction.DESC, "bookmarkCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        PageRequest pageRequest = PageRequest.of(page, size, sortOption);
        Page<PostResponse> posts = postService.getAllPosts(user.getId(), pageRequest);
        return ResponseTemplate.ok(posts);
    }

    @Operation(
            summary = "Get My Reviews",
            description = "내 댓글 조회"
    )
    @GetMapping("/reviews")
    public ResponseTemplate<Page<MyReviewDto>> getMyPageReviews(@AuthenticationPrincipal User user,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseTemplate.ok(myPageService.getMyReviews(user.getId(), pageRequest));
    }

    public record MyReviewDto(
            PostInfo post,
            ReviewInfo review
    ) {
        public static MyReviewDto of(Review review, List<Image> images) {
            return new MyReviewDto(
                    PostInfo.of(review.getPost()),
                    ReviewInfo.of(review, images)
            );
        }
    }

    public record PostInfo (
            Long id,
            String title
    ) {
        public static PostInfo of(Post post) {
            return new PostInfo(post.getId(), post.getTitle());
        }
    }

    public record ReviewInfo (
            Long id,
            String content,
            String image,
            LocalDateTime createdAt
    ) {
        public static ReviewInfo of(Review review, List<Image> images) {
            return new ReviewInfo(
                    review.getId(),
                    review.getContent(),
                    images.size() == 0 ? ImageUtils.getDefaultImageUrl() : images.get(0).getUrl(),
                    review.getCreatedAt()
            );
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetMyPageRequest {

        @Length(min = 8, max = 12)
        @Pattern(regexp = "^[A-Za-z0-9]+$")
        private String password;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetMyPageResponse {
        private String loginId;
        private String name;
        private String birth;
        private String email;
        private String nickname;
        private Image image;
        private Boolean emailNotify;
        private Boolean smsNotify;
        private Boolean postLikeAlarm;
        private Boolean postReviewAlarm;
        private Boolean popularPostAlarm;

        public static GetMyPageResponse of(User user) {
            GetMyPageResponseBuilder builder = GetMyPageResponse.builder()
                    .loginId(user.getLoginId())
                    .name(user.getName())
                    .birth(DateUtils.yyMMdd(user.getBirthdate()))
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .emailNotify(user.isEmailNotify())
                    .smsNotify(user.isSmsNotify())
                    .postLikeAlarm(user.isPopular_post_alarm())
                    .postReviewAlarm(user.isPostReviewAlarm())
                    .popularPostAlarm(user.isPopular_post_alarm())
                    ;

            if (Objects.nonNull(user.getProfileImage())) {
                builder.image(
                        Image.builder()
                                .id(user.getProfileImage().getId())
                                .src(user.getProfileImage().getUrl())
                                .build()
                );
            }
            return builder.build();
        }

        @Getter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Image {
            private Long id;
            private String src;
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetMyPageNotificationResponse {
        private Boolean eventNotification;
        private Boolean pushNotification;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UpdateNotificationRequest {

        @NotNull
        Boolean event;

        @NotNull
        Boolean alert;
    }
}
