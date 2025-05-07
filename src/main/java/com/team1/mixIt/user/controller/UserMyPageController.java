package com.team1.mixIt.user.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.user.entity.User;
import com.team1.mixIt.user.service.UserService;
import com.team1.mixIt.utils.DateUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Tag(name = "User My Page")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/my-page")
public class UserMyPageController {

    private final UserService userService;

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
            summary = "Get Notification Information",
            description = "MyPage 하위 알림 정보 조회 API"
    )
    @GetMapping("/notification")
    public ResponseTemplate<GetMyPageNotificationResponse> getMyPageNotification(
            @AuthenticationPrincipal User user
    ) {
        return ResponseTemplate.ok(
                GetMyPageNotificationResponse.builder()
                        .eventNotification(user.isNotifyOn())
                        .pushNotification(user.isNotifyOn())
                        .build()
        );
    }

    @Operation(
            summary = "Update Notification Information",
            description = "MyPage 하위 알림 정보 수정 API"
    )
    @PostMapping("/notification")
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

        public static GetMyPageResponse of(User user) {
            GetMyPageResponseBuilder builder = GetMyPageResponse.builder()
                    .loginId(user.getLoginId())
                    .name(user.getName())
                    .birth(DateUtils.yyMMdd(user.getBirthdate()))
                    .email(user.getEmail())
                    .nickname(user.getNickname());

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
