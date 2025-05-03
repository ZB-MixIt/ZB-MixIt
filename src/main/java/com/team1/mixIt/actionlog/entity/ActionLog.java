// src/main/java/com/team1/mixIt/log/entity/ActionLog.java
package com.team1.mixIt.actionlog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "action_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActionLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType;//bookmark, like, view

    @Column(name = "action_time", nullable = false)
    @Builder.Default
    private LocalDateTime actionTime = LocalDateTime.now();
}
