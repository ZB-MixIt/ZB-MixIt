package com.team1.mixIt.notification.controller;

import com.team1.mixIt.notification.sse.NotificationSseService;
import com.team1.mixIt.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "알림 API", description = "알림 관련 기능을 처리하는 API")
public class NotificationController {

    private final NotificationSseService sseService;

    @Operation(
            summary = "알림 구독 시작 (SSE)",
            description = "사용자가 알림을 실시간으로 구독할 수 있는 SSE 연결을 시작합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "SSE 연결 성공"),
                    @ApiResponse(responseCode = "500", description = "서버 오류 발생")
            }
    )
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal User user) {
        return sseService.subscribe(user.getId());
    }
}
