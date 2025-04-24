package com.team1.mixIt.tag.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "tag_stats")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class TagStats {
    @Id
    @Column(length=50)
    private String tag;

    @Column(name = "use_count", nullable = false)
    private Long useCount = 0L;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
