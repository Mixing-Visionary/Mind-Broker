package ru.visionary.mixing.mind_broker.entity;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record Follow(
        User follower,
        User follow,
        LocalDateTime followAt
) {}
