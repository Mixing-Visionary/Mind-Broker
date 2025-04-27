package ru.visionary.mixing.mind_broker.entity;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record Image (
    UUID id,
    User owner,
    Protection protection,
    LocalDateTime createdAt
) {}
