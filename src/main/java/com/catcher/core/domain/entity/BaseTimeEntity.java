package com.catcher.core.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@MappedSuperclass
@Getter
public class BaseTimeEntity {
    public static ZoneId zoneId = ZoneId.of("Asia/Seoul");

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = ZonedDateTime.now(zoneId);
        this.updatedAt = ZonedDateTime.now(zoneId);
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = ZonedDateTime.now(zoneId);
    }
}
