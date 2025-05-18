package ru.visionary.mixing.mind_broker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import ru.visionary.mixing.mind_broker.config.properties.ProcessingProperties;
import ru.visionary.mixing.mind_broker.config.properties.RabbitProperties;
import ru.visionary.mixing.mind_broker.entity.ProcessingMessage;
import ru.visionary.mixing.mind_broker.entity.ProcessingResultMessage;
import ru.visionary.mixing.mind_broker.entity.ProcessingStatus;
import ru.visionary.mixing.mind_broker.repository.ProcessingRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessingListenerTest {
    @Mock
    private ProcessingRepository processingRepository;
    @Mock
    private AmqpTemplate amqpTemplate;
    @Mock
    private RabbitProperties rabbitProperties;
    @Mock
    private ProcessingProperties processingProperties;

    @InjectMocks
    private ProcessingListener processingListener;

    @Test
    void processImage_WhenQueueExpires_ShouldUpdateProcessingToCanceled() {
        ProcessingMessage message = new ProcessingMessage(
                UUID.randomUUID(), "image", "style", BigDecimal.valueOf(0.8)
        );
        when(processingRepository.getStartTimeById(any()))
                .thenReturn(LocalDateTime.now());
        when(rabbitProperties.processedQueueExpires()).thenReturn(0);

        processingListener.processImage(message);

        verify(processingRepository).updateStatus(message.uuid(), ProcessingStatus.CANCELED);
        verify(rabbitProperties, never()).processedExchange();
    }

    @Test
    void processImage_WhenTimeoutExpired_ShouldCancelProcessing() {
        UUID uuid = UUID.randomUUID();
        ProcessingMessage message = new ProcessingMessage(uuid, "image", "style", BigDecimal.valueOf(0.8));

        LocalDateTime oldStartTime = LocalDateTime.now().minusMinutes(11);
        when(processingRepository.getStartTimeById(uuid)).thenReturn(oldStartTime);

        when(rabbitProperties.processedQueueExpires()).thenReturn(1800000);
        when(processingProperties.maxTimeFromStart()).thenReturn(Duration.parse("PT10M"));
        when(rabbitProperties.processedExchange()).thenReturn("exchange");

        processingListener.processImage(message);

        verify(processingRepository).updateStatus(uuid, ProcessingStatus.CANCELED);
        verify(amqpTemplate).convertAndSend(eq("exchange"), eq(uuid.toString()), any(ProcessingResultMessage.class));
    }

    @Test
    void processImage_WhenProcessingFails_ShouldUpdateStatusToFailed() {
        ProcessingMessage message = new ProcessingMessage(
                UUID.randomUUID(), "image", "style", BigDecimal.valueOf(0.8)
        );
        when(processingRepository.getStartTimeById(any()))
                .thenReturn(LocalDateTime.now());
        when(rabbitProperties.processedQueueExpires()).thenReturn(1800000);
        when(processingProperties.maxTimeFromStart()).thenReturn(Duration.parse("PT10M"));

        processingListener.processImage(message);

        verify(processingRepository).updateStatus(message.uuid(), ProcessingStatus.FAILED);
    }
}