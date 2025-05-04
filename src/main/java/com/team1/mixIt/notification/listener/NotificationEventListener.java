package com.team1.mixIt.notification.listener;

import com.team1.mixIt.notification.entity.Notification;
import com.team1.mixIt.notification.event.NotificationEvent;
import com.team1.mixIt.notification.repository.NotificationRepository;
import com.team1.mixIt.notification.sse.NotificationSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final NotificationRepository notificationRepo;
    private final NotificationSseService   sseService;

    @Async
    @EventListener
    public void onNotificationEvent(NotificationEvent ev) {
        Notification n = Notification.builder()
                .receiverId(ev.getReceiverId())
                .type(ev.getType())
                .entityId(ev.getEntityId())
                .message(ev.getMessage())
                .build();
        notificationRepo.save(n);

        sseService.send(ev.getReceiverId(), Map.of(
                "id", n.getId(),
                "type", n.getType(),
                "entityId", n.getEntityId(),
                "message", n.getMessage(),
                "createdAt", n.getCreatedAt()
        ));
    }
}
