package ru.visionary.mixing.mind_broker.job;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.visionary.mixing.mind_broker.config.properties.ProcessingProperties;
import ru.visionary.mixing.mind_broker.entity.Processing;
import ru.visionary.mixing.mind_broker.entity.ProcessingResultMessage;
import ru.visionary.mixing.mind_broker.entity.ProcessingStatus;
import ru.visionary.mixing.mind_broker.repository.ProcessingRepository;
import ru.visionary.mixing.mind_broker.websocket.ProcessingWebSocketHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelLongProcessingJobTest {

    @Mock
    private ProcessingRepository processingRepository;

    @Mock
    private ProcessingProperties processingProperties;

    @Mock
    private ProcessingWebSocketHandler processingWebSocketHandler;

    @InjectMocks
    private CancelLongProcessingJob cancelLongProcessingJob;

    @Test
    void cancelLongProcessingJob_ShouldCancelAndNotify() {
        Processing processing = Processing.builder()
                .id(UUID.randomUUID())
                .status(ProcessingStatus.PROCESSING)
                .build();

        when(processingProperties.maxProcessingTimeMinutes()).thenReturn(5);
        when(processingRepository.cancelLongProcessing(any(LocalDateTime.class)))
                .thenReturn(List.of(processing));

        cancelLongProcessingJob.cancelLongProcessingJob();

        verify(processingRepository).cancelLongProcessing(any(LocalDateTime.class));
        verify(processingWebSocketHandler).sendResult(
                new ProcessingResultMessage(null, ProcessingStatus.CANCELED, null, null),
                processing.id(),
                true
        );
    }

    @Test
    void cancelLongProcessingJob_NoProcessingToCancel_ShouldDoNothing() {
        when(processingProperties.maxProcessingTimeMinutes()).thenReturn(5);
        when(processingRepository.cancelLongProcessing(any(LocalDateTime.class)))
                .thenReturn(List.of());

        cancelLongProcessingJob.cancelLongProcessingJob();

        verify(processingRepository).cancelLongProcessing(any(LocalDateTime.class));
        verifyNoInteractions(processingWebSocketHandler);
    }
}