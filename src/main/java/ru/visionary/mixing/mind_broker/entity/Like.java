package ru.visionary.mixing.mind_broker.entity;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record Like(
        User user,
        Image image,
        LocalDateTime likeAt
) {}
