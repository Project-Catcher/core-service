package com.catcher.core.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.ZonedDateTime;

@MappedSuperclass
@Getter
public class BaseTimeEntity {
    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = ZonedDateTime.now();
    }
}
