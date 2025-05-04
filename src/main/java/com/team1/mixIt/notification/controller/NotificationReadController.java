package com.team1.mixIt.notification.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.notification.entity.Notification;
import com.team1.mixIt.notification.service.NotificationService;
import com.team1.mixIt.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationReadController {
    private final NotificationService notificationService;

    @Operation(summary = "읽지 않은 알림 목록 조회")
    @GetMapping
    public ResponseTemplate<List<Notification>> getUnread(
            @AuthenticationPrincipal User user
    ) {
        return ResponseTemplate.ok(
                notificationService.getUnreadNotifications(user.getId())
        );
    }

    @Operation(summary = "알림 읽음 처리")
    @PatchMapping("/{id}/read")
    public ResponseTemplate<Void> markAsRead(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        notificationService.markAsRead(user.getId(), id);
        return ResponseTemplate.ok();
    }
}
