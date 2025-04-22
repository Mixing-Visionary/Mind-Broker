package ru.visionary.mixing.mind_broker.config.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private final String secret;
    private final String accessTokenExpiration;
    private final String refreshTokenExpiration;
}
