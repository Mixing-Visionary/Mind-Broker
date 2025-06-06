package ru.visionary.mixing.mind_broker.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties (
    String secret,
    Duration accessTokenExpiration,
    Duration refreshTokenExpiration
) {}
