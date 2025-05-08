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
@Tag(name = "알림 조회 API", description = "사용자가 받은 알림 목록을 조회하는 API")
public class NotificationQueryController {

    private final NotificationService notificationService;

    @Operation(
            summary = "내가 받은 알림 목록 조회",
            description = "사용자가 받은 알림 목록을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
                    @ApiResponse(responseCode = "404", description = "알림이 존재하지 않음")
            }
    )
    @GetMapping
    public ResponseTemplate<List<NotificationDto>> list(@AuthenticationPrincipal User user) {
        return ResponseTemplate.ok(
                notificationService.getNotificationsFor(user.getId())
        );
    }
}
