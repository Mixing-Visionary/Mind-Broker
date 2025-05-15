package ru.visionary.mixing.mind_broker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.visionary.mixing.generated.model.ProcessingImageResponse;
import ru.visionary.mixing.mind_broker.config.properties.CompressionProperties;
import ru.visionary.mixing.mind_broker.config.properties.ProcessingProperties;
import ru.visionary.mixing.mind_broker.config.properties.RabbitProperties;
import ru.visionary.mixing.mind_broker.entity.*;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.repository.ProcessingRepository;
import ru.visionary.mixing.mind_broker.repository.StyleRepository;
import ru.visionary.mixing.mind_broker.utils.ImageUtils;
import ru.visionary.mixing.mind_broker.utils.SecurityContextUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessingService {
    private final ProcessingRepository processingRepository;
    private final StyleRepository styleRepository;
    private final AmqpTemplate amqpTemplate;
    private final RabbitProperties rabbitProperties;
    private final ProcessingProperties processingProperties;
    private final RabbitService rabbitService;

    public ProcessingImageResponse processImage(MultipartFile image, String style, BigDecimal strength) {
        ImageUtils.checkImage(image);
        log.info("Starting image processing - Style: {}, Strength: {}, Original Size: {} bytes", style, strength, image.getSize());

        User user = SecurityContextUtils.getAuthenticatedUser();
        if (user == null) {
            log.error("Processing error: user not authorized");
            throw new ServiceException(ErrorCode.USER_NOT_AUTHORIZED);
        }
        if (!user.active()) {
            log.error("Processing error: user is inactive");
            throw new ServiceException(ErrorCode.CURRENT_USER_DELETED);
        }

        Style styleEntity = styleRepository.findByName(style);
        if (styleEntity == null) {
            log.warn("Processing error: Style '{}' not found", style);
            throw new ServiceException(ErrorCode.STYLE_NOT_FOUND);
        }
        if (!styleEntity.active()) {
            log.warn("Processing error: Style '{}' is not active", style);
            throw new ServiceException(ErrorCode.STYLE_NOT_SUPPORTED);
        }

        String base64Image = convertImage(image);
        log.debug("Image converted to base64 - Size: {} characters", base64Image.length());

        UUID id = UUID.randomUUID();
        Processing processing = Processing.builder()
                .id(id)
                .user(user)
                .style(styleEntity)
                .build();
        processingRepository.save(processing);
        log.info("Processing task created - ID: {}, User: {}, Style: {}", id, user.id(), styleEntity.id());

        try {
            rabbitService.createResponseQueue(id);
            log.debug("Response queue created for processing ID: {}", id);

            amqpTemplate.convertAndSend(
                    rabbitProperties.processingExchange(),
                    "",
                    new ProcessingMessage(id, base64Image, style, strength)
            );
            log.info("Processing task sent to queue - ID: {}, Exchange: {}", id, rabbitProperties.processingExchange());
        } catch (Exception e) {
            log.error("Failed to send processing task to queue - ID: {}, Error: {}", id, e.getMessage());
            processingRepository.updateStatus(id, ProcessingStatus.FAILED);
            throw new ServiceException(ErrorCode.FAILED_PUSH_TO_RABBIT);
        }

        return new ProcessingImageResponse(id);
    }

    private String convertImage(MultipartFile image) {
        byte[] imageBytes;
        try {
            imageBytes = image.getBytes();
            if (processingProperties.compression().enabled()) {
                log.debug("Attempting image compression - Original Size: {} bytes", imageBytes.length);
                try {
                    imageBytes = compressImage(imageBytes);
                } catch (IOException ignored) {}
            }
        } catch (IOException e) {
            log.error("Image conversion error: {}", e.getMessage());
            throw new ServiceException(ErrorCode.INVALID_REQUEST);
        }

        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private byte[] compressImage(byte[] original) throws IOException {
        CompressionProperties compressionProperties = processingProperties.compression();

        if (original.length < compressionProperties.minLength()) {
            return original;
        }

        try (InputStream is = new ByteArrayInputStream(original);
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            Thumbnails.of(is)
                    .size(compressionProperties.maxWidth(), compressionProperties.maxHeight())
                    .outputFormat("jpg")
                    .outputQuality(compressionProperties.quality())
                    .toOutputStream(os);

            byte[] compressed = os.toByteArray();

            log.debug("Image compressed from {} to {} bytes ({}% ratio)", original.length, compressed.length, (compressed.length * 100) / original.length);

            return compressed;
        }
    }
}
