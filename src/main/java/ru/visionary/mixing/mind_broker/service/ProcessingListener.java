package ru.visionary.mixing.mind_broker.service;

import feign.Request.Options;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import ru.visionary.mixing.mind_broker.client.MegamindClient;
import ru.visionary.mixing.mind_broker.client.dto.ImageProcessingRequest;
import ru.visionary.mixing.mind_broker.client.dto.ImageProcessingResponse;
import ru.visionary.mixing.mind_broker.config.properties.MegamindProperties;
import ru.visionary.mixing.mind_broker.config.properties.ProcessingProperties;
import ru.visionary.mixing.mind_broker.config.properties.RabbitProperties;
import ru.visionary.mixing.mind_broker.entity.ProcessingMessage;
import ru.visionary.mixing.mind_broker.entity.ProcessingResultMessage;
import ru.visionary.mixing.mind_broker.entity.ProcessingStatus;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.repository.ProcessingRepository;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessingListener {
    private final ProcessingRepository processingRepository;
    private final ProcessingProperties processingProperties;
    private final MegamindProperties megamindProperties;
    private final RabbitProperties rabbitProperties;
    private final AmqpTemplate amqpTemplate;
    private final MegamindClient megamindClient;

    @RabbitListener(queues = "${app.rabbit.processing-queue}")
    public void processImage(ProcessingMessage message) {
        log.info("Processing started - ID: {}, Style: {}", message.uuid(), message.style());
        LocalDateTime startTime = processingRepository.getStartTimeById(message.uuid());
        LocalDateTime now = LocalDateTime.now();

        if (startTime.plusSeconds(rabbitProperties.processedQueueExpires() / 1000).minusMinutes(1).isBefore(now)) {
            // Очередь скоро удалится либо уже удалилась, поэтому просто обновим статус в БД
            processingRepository.updateStatus(message.uuid(), ProcessingStatus.CANCELED);
            return;
        }
        if (startTime.plusMinutes(processingProperties.maxTimeFromStart().toMinutes()).isBefore(now)) {
            // Время на обработку вышло, отправим ответ о том, что операция пропущена
            processingRepository.updateStatus(message.uuid(), ProcessingStatus.CANCELED);
            try {
                amqpTemplate.convertAndSend(
                        rabbitProperties.processedExchange(),
                        message.uuid().toString(),
                        new ProcessingResultMessage(null, ProcessingStatus.CANCELED, null, null)
                );
            } catch (Exception ignored) {}
            return;
        }

        try {
            log.debug("Sending processing status update - ID: {}, Status: PROCESSING", message.uuid());
            amqpTemplate.convertAndSend(
                    rabbitProperties.processedExchange(),
                    message.uuid().toString(),
                    new ProcessingResultMessage(null, ProcessingStatus.PROCESSING, null, null)
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

            amqpTemplate.convertAndSend(
                    rabbitProperties.processedExchange(),
                    message.uuid().toString(),
                    new ProcessingResultMessage(processed.processed_image(), ProcessingStatus.COMPLETED, null, null)
            );
            processingRepository.updateStatus(message.uuid(), ProcessingStatus.COMPLETED);
        } catch (ServiceException se) {
            log.error("Processing failed - ID: {}, Error: {}", message.uuid(), se.getErrorCode().getMessage());
            processingRepository.updateStatus(message.uuid(), ProcessingStatus.FAILED);
            ErrorCode errorCode = se.getErrorCode();
            try {
                amqpTemplate.convertAndSend(
                        rabbitProperties.processedExchange(),
                        message.uuid().toString(),
                        new ProcessingResultMessage(null, ProcessingStatus.FAILED, errorCode.getErrorCode(), errorCode.getMessage())
                );
            } catch (Exception ignored) {}
        } catch (AmqpException ae) {
            log.error("Unexpected processing error - ID: {}, Error: {}", message.uuid(), ae.getMessage());
            processingRepository.updateStatus(message.uuid(), ProcessingStatus.FAILED);
            ErrorCode errorCode = ErrorCode.RABBIT_ERROR;
            try {
                amqpTemplate.convertAndSend(
                        rabbitProperties.processedExchange(),
                        message.uuid().toString(),
                        new ProcessingResultMessage(null, ProcessingStatus.FAILED, errorCode.getErrorCode(), errorCode.getMessage())
                );
            } catch (Exception ignored) {}
        } catch (Exception e) {
            log.error("Unexpected processing error - ID: {}, Error: {}", message.uuid(), e.getMessage());
            processingRepository.updateStatus(message.uuid(), ProcessingStatus.FAILED);
            ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
            try {
                amqpTemplate.convertAndSend(
                        rabbitProperties.processedExchange(),
                        message.uuid().toString(),
                        new ProcessingResultMessage(null, ProcessingStatus.FAILED, errorCode.getErrorCode(), errorCode.getMessage())
                );
            } catch (Exception ignored) {}
        }
    }
}
