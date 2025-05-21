package ru.visionary.mixing.mind_broker.service;

import feign.Request.Options;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
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

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessingListener {
    private final ProcessingRepository processingRepository;
    private final ProcessingProperties processingProperties;
    private final MegamindProperties megamindProperties;
    private final MegamindClient megamindClient;
    private final ProcessingWebSocketHandler processingWebSocketHandler;

    @RabbitListener(queues = "${app.rabbit.processing-queue}")
    public void processImage(ProcessingMessage message) {
        log.info("Processing started - ID: {}, Style: {}", message.uuid(), message.style());
        LocalDateTime startTime = processingRepository.getStartTimeById(message.uuid());
        LocalDateTime now = LocalDateTime.now();

        if (startTime.plusMinutes(processingProperties.maxTimeFromStart().toMinutes()).isBefore(now)) {
            processingRepository.updateStatus(message.uuid(), ProcessingStatus.CANCELED);
            processingWebSocketHandler.sendResult(
                    new ProcessingResultMessage(null, ProcessingStatus.CANCELED, null, null),
                    message.uuid(),
                    true
            );
            return;
        }

        try {
            log.debug("Sending processing status update - ID: {}, Status: PROCESSING", message.uuid());
            processingWebSocketHandler.sendResult(
                    new ProcessingResultMessage(null, ProcessingStatus.PROCESSING, null, null),
                    message.uuid(),
                    false
            );
            processingRepository.updateStatus(message.uuid(), ProcessingStatus.PROCESSING);

            Options options = new Options(
                    megamindProperties.processingTimeout(), TimeUnit.SECONDS, megamindProperties.processingTimeout(), TimeUnit.SECONDS, false
            );

            log.debug("Calling Megamind API - ID: {}, Timeout: {}s", message.uuid(), megamindProperties.processingTimeout());

            ImageProcessingResponse processed = megamindClient.process(
                    options, new ImageProcessingRequest(message.base64Image(), message.style(), message.strength(), megamindProperties.apiKey())
            );

            log.info("Megamind processing completed - ID: {}, Processing Time: {}", message.uuid(), processed.processing_time());

            processingWebSocketHandler.sendResult(
                    new ProcessingResultMessage(processed.processed_image(), ProcessingStatus.COMPLETED, null, null),
                    message.uuid(),
                    true
            );
            processingRepository.updateStatus(message.uuid(), ProcessingStatus.COMPLETED);
        } catch (ServiceException se) {
            log.error("Processing failed - ID: {}, Error: {}", message.uuid(), se.getErrorCode().getMessage());
            processingRepository.updateStatus(message.uuid(), ProcessingStatus.FAILED);
            ErrorCode errorCode = se.getErrorCode();
            processingWebSocketHandler.sendResult(
                    new ProcessingResultMessage(null, ProcessingStatus.FAILED, errorCode.getErrorCode(), errorCode.getMessage()),
                    message.uuid(),
                    true
            );
        } catch (Exception e) {
            log.error("Unexpected processing error - ID: {}, Error: {}", message.uuid(), e.getMessage());
            processingRepository.updateStatus(message.uuid(), ProcessingStatus.FAILED);
            ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
            processingWebSocketHandler.sendResult(
                    new ProcessingResultMessage(null, ProcessingStatus.FAILED, errorCode.getErrorCode(), errorCode.getMessage()),
                    message.uuid(),
                    true
            );
        }
    }
}
