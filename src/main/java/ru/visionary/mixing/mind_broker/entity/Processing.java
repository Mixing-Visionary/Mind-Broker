package ru.visionary.mixing.mind_broker.entity;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record Processing(
        UUID id,
        User user,
        Style style,
        LocalDateTime startTime,
        ProcessingStatus status,
        LocalDateTime statusAt
) {}
