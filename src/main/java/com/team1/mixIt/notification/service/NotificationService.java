package com.team1.mixIt.notification.service;

import com.team1.mixIt.notification.dto.NotificationDto;
import com.team1.mixIt.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepo;

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsFor(Long userId) {
        return notificationRepo.findByReceiverIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(n -> new NotificationDto(
                        n.getId(), n.getType(), n.getEntityId(),
                        n.getMessage(), n.getCreatedAt(), n.isRead()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotifications(Long userId) {
        return notificationRepo.findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationDto::fromEntity)
                .toList();
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        var n = notificationRepo.findByIdAndReceiverId(notificationId, userId)
                .orElseThrow(() -> new RuntimeException("알림이 없거나 권한이 없습니다."));
        n.setRead(true);
    }
}
