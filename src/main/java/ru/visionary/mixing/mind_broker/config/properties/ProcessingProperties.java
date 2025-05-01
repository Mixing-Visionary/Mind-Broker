package ru.visionary.mixing.mind_broker.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.processing")
public record ProcessingProperties(
        Duration maxTimeFromStart,
        CompressionProperties compression
) {}
