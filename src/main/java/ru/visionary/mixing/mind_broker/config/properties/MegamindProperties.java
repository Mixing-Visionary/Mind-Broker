package ru.visionary.mixing.mind_broker.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.megamind")
public record MegamindProperties(
        String apiKey,
        Integer processingTimeout,
        String updateStylesJobCron
) {}
