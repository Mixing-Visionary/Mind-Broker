package ru.visionary.mixing.mind_broker.config.properties;

public record CompressionProperties(
        boolean enabled,
        double quality,
        int maxWidth,
        int maxHeight,
        int minLength
) {}
