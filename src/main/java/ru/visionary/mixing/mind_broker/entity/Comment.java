package ru.visionary.mixing.mind_broker.entity;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record Comment(
        Long id,
        User author,
        Image image,
        String comment,
        LocalDateTime createdAt
) {}
