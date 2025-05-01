package ru.visionary.mixing.mind_broker.entity;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record ProcessingMessage(
        UUID uuid,
        String base64Image,
        String style,
        BigDecimal strength
) {}
