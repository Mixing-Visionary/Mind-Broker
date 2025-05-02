package ru.visionary.mixing.mind_broker.client.dto;

import java.math.BigDecimal;

public record ImageProcessingResponse(
        String processed_image,
        BigDecimal processing_time,
        String style
) {}
