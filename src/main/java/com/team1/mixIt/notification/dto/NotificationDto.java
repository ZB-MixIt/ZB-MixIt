package com.team1.mixIt.notification.dto;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class NotificationDto {
    Long id;
    String type;
    Long entityId;
    String message;
    LocalDateTime createdAt;
    boolean isRead;

    public static NotificationDto fromEntity(com.team1.mixIt.notification.entity.Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getType(),
                n.getEntityId(),
                n.getMessage(),
                n.getCreatedAt(),
                n.isRead()
        );
    }
}