package com.team1.mixIt.notification.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.notification.dto.NotificationDto;
import com.team1.mixIt.notification.service.NotificationService;
import com.team1.mixIt.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "알림 조회 API", description = "사용자가 받은 알림을 조회하거나 읽음 상태로 처리하는 API")
public class NotificationReadController {

    private final NotificationService notificationService;

    @Operation(
            summary = "읽지 않은 알림 목록 조회",
            description = "사용자가 읽지 않은 알림 목록을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "읽지 않은 알림 목록 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
            }
    )
    @GetMapping("/unread")
    public ResponseTemplate<List<NotificationDto>> getUnread(
            @AuthenticationPrincipal User user
    ) {
        return ResponseTemplate.ok(
                notificationService.getUnreadNotifications(user.getId())
        );
    }

    @Operation(
            summary = "알림 읽음 처리",
            description = "특정 알림을 읽음 상태로 처리합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
                    @ApiResponse(responseCode = "404", description = "알림이 없거나 권한 없음")
            }
    )
    @PatchMapping("/{id}/read")
    public ResponseTemplate<Void> markAsRead(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        notificationService.markAsRead(user.getId(), id);
        return ResponseTemplate.ok();
    }
}
