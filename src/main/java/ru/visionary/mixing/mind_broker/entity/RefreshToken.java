package ru.visionary.mixing.mind_broker.entity;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RefreshToken (
    Long id,
    String token,
    User user,
    LocalDateTime expiryDate
) {}
