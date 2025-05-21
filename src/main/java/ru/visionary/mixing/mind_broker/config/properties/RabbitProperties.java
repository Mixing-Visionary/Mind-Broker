package ru.visionary.mixing.mind_broker.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbit")
public record RabbitProperties (
        String processingExchange,
        String processingQueue
) {}
