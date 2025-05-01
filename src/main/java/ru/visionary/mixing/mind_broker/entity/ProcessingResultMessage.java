package ru.visionary.mixing.mind_broker.entity;

import lombok.Builder;

@Builder
public record ProcessingResultMessage(
        String base64Image,
        ProcessingStatus processingStatus,
        Integer errorCode,
        String errorMessage
) {}
