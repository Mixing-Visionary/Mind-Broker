package ru.visionary.mixing.mind_broker.config.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "app.minio")
public class MinioProperties {
    private final String endpoint;
    private final String accessKey;
    private final String secretKey;
    private final String bucketName;
}
