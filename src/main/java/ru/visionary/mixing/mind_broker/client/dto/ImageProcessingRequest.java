package ru.visionary.mixing.mind_broker.client.dto;

import java.math.BigDecimal;

public record ImageProcessingRequest(
        String image,
        String style,
        BigDecimal strength,
        String api_key
) {}
