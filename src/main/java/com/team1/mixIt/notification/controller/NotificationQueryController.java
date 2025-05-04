package com.team1.mixIt.notification.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.notification.dto.NotificationDto;
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
public class NotificationQueryController {
    private final NotificationService notificationService;

    @Operation(summary = "내가 받은 알림 목록 조회")
    @GetMapping
    public ResponseTemplate<List<NotificationDto>> list(
            @AuthenticationPrincipal User user
    ) {
        return ResponseTemplate.ok(
                notificationService.getNotificationsFor(user.getId())
        );
    }
}
