package ru.visionary.mixing.mind_broker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.visionary.mixing.mind_broker.client.MegamindClient;
import ru.visionary.mixing.mind_broker.client.dto.ImageProcessingRequest;
import ru.visionary.mixing.mind_broker.client.dto.ImageProcessingResponse;
import ru.visionary.mixing.mind_broker.config.properties.MegamindProperties;
import ru.visionary.mixing.mind_broker.config.properties.ProcessingProperties;
import ru.visionary.mixing.mind_broker.entity.ProcessingMessage;
import ru.visionary.mixing.mind_broker.entity.ProcessingResultMessage;
import ru.visionary.mixing.mind_broker.entity.ProcessingStatus;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.repository.ProcessingRepository;
import ru.visionary.mixing.mind_broker.websocket.ProcessingWebSocketHandler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessingListenerTest {

    @Mock
    private ProcessingRepository processingRepository;
    @Mock
    private ProcessingProperties processingProperties;
    @Mock
    private MegamindProperties megamindProperties;
    @Mock
    private MegamindClient megamindClient;
    @Mock
    private ProcessingWebSocketHandler processingWebSocketHandler;

    @InjectMocks
    private ProcessingListener processingListener;

    private ProcessingMessage testMessage;
    private final UUID testUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        testMessage = new ProcessingMessage(
                testUuid,
                "base64Image",
                "test-style",
                BigDecimal.valueOf(0.5)
        );

        lenient().when(processingProperties.maxTimeFromStart()).thenReturn(java.time.Duration.ofMinutes(10));
        lenient().when(megamindProperties.processingTimeout()).thenReturn(30);
        lenient().when(megamindProperties.apiKey()).thenReturn("test-api-key");
    }

    @Test
    void processImage_SuccessfulProcessing_ShouldUpdateStatusAndSendResult() throws Exception {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(1);
        ImageProcessingResponse mockResponse = new ImageProcessingResponse(
                "processed-image",
                BigDecimal.valueOf(2.5),
                "test-style"
        );

        when(processingRepository.getStartTimeById(testUuid)).thenReturn(startTime);
        when(megamindClient.process(any(), any())).thenReturn(mockResponse);

        processingListener.processImage(testMessage);

        verify(processingRepository).updateStatus(testUuid, ProcessingStatus.PROCESSING);
        verify(processingRepository).updateStatus(testUuid, ProcessingStatus.COMPLETED);
        verify(processingWebSocketHandler).sendResult(
                new ProcessingResultMessage(
                        "processed-image",
                        ProcessingStatus.COMPLETED,
                        null,
                        null
                ),
                testUuid,
                true
        );
    }

    @Test
    void processImage_MegamindError_ShouldUpdateStatusToFailed() {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(1);
        when(processingRepository.getStartTimeById(testUuid)).thenReturn(startTime);
        when(megamindClient.process(any(), any()))
                .thenThrow(new ServiceException(ErrorCode.MEGAMIND_ERROR));

        processingListener.processImage(testMessage);

        verify(processingRepository).updateStatus(testUuid, ProcessingStatus.FAILED);
        verify(processingWebSocketHandler).sendResult(
                argThat(result -> result.processingStatus() == ProcessingStatus.FAILED),
                eq(testUuid),
                eq(true)
        );
    }

    @Test
    void processImage_ExceededMaxProcessingTime_ShouldCancelProcessing() {
        LocalDateTime oldStartTime = LocalDateTime.now().minusMinutes(11);
        when(processingRepository.getStartTimeById(testUuid)).thenReturn(oldStartTime);

        processingListener.processImage(testMessage);

        verify(processingRepository).updateStatus(testUuid, ProcessingStatus.CANCELED);
        verify(processingWebSocketHandler).sendResult(
                argThat(result -> result.processingStatus() == ProcessingStatus.CANCELED),
                eq(testUuid),
                eq(true)
        );
        verify(megamindClient, never()).process(any(), any());
    }

    @Test
    void processImage_GeneralException_ShouldHandleAsInternalError() {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(1);
        when(processingRepository.getStartTimeById(testUuid)).thenReturn(startTime);
        when(megamindClient.process(any(), any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        processingListener.processImage(testMessage);

        verify(processingRepository).updateStatus(testUuid, ProcessingStatus.FAILED);
        verify(processingWebSocketHandler).sendResult(
                argThat(result ->
                        result.processingStatus() == ProcessingStatus.FAILED &&
                                result.errorCode() == ErrorCode.INTERNAL_ERROR.getErrorCode()
                ),
                eq(testUuid),
                eq(true)
        );
    }

    @Test
    void processImage_ShouldSendProcessingStatusUpdate() {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(1);
        when(processingRepository.getStartTimeById(testUuid)).thenReturn(startTime);

        processingListener.processImage(testMessage);

        verify(processingWebSocketHandler).sendResult(
                new ProcessingResultMessage(null, ProcessingStatus.PROCESSING, null, null),
                testUuid,
                false
        );
    }

    @Test
    void processImage_ShouldUseCorrectRequestParameters() throws Exception {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(1);
        when(processingRepository.getStartTimeById(testUuid)).thenReturn(startTime);

        ArgumentCaptor<ImageProcessingRequest> requestCaptor =
                ArgumentCaptor.forClass(ImageProcessingRequest.class);

        processingListener.processImage(testMessage);

        verify(megamindClient).process(
                any(),
                requestCaptor.capture()
        );

        ImageProcessingRequest actualRequest = requestCaptor.getValue();
        assertEquals("base64Image", actualRequest.image());
        assertEquals("test-style", actualRequest.style());
        assertEquals(BigDecimal.valueOf(0.5), actualRequest.strength());
        assertEquals("test-api-key", actualRequest.api_key());
    }
}