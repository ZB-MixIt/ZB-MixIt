// src/main/java/com/team1/mixIt/notification/event/NotificationEvent.java
package com.team1.mixIt.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NotificationEvent extends ApplicationEvent {
    private final Long   receiverId;
    private final String type;
    private final Long   entityId;
    private final String message;

    public NotificationEvent(Object source,
                             Long receiverId,
                             String type,
                             Long entityId,
                             String message) {
        super(source);
        this.receiverId = receiverId;
        this.type       = type;
        this.entityId   = entityId;
        this.message    = message;
    }
}
