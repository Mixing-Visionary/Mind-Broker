package ru.visionary.mixing.mind_broker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.visionary.mixing.generated.api.ProcessingApi;
import ru.visionary.mixing.generated.model.ProcessingImageResponse;
import ru.visionary.mixing.generated.model.ProcessingStatusResponse;
import ru.visionary.mixing.generated.model.StylesResponse;
import ru.visionary.mixing.mind_broker.service.ProcessingService;
import ru.visionary.mixing.mind_broker.service.StyleService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProcessingController implements ProcessingApi {
    private final ProcessingService processingService;
    private final StyleService styleService;

    @Override
    public ResponseEntity<ProcessingImageResponse> processing(String style, MultipartFile image, BigDecimal strength) {
        return ResponseEntity.ok(processingService.processImage(image, style, strength));
    }

    @Override
    public ResponseEntity<ProcessingStatusResponse> getStatus(UUID uuid) {
        return ResponseEntity.ok(processingService.getStatus(uuid));
    }

    @Override
    public ResponseEntity<StylesResponse> styles() {
        return ResponseEntity.ok(styleService.getStyles());
    }
}
