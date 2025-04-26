package ru.visionary.mixing.mind_broker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.visionary.mixing.generated.api.ImageApi;
import ru.visionary.mixing.generated.model.SaveImageResponse;
import ru.visionary.mixing.mind_broker.service.ImageService;

@RestController
@RequiredArgsConstructor
public class ImageController implements ImageApi {
    private final ImageService imageService;

    @Override
    public ResponseEntity<SaveImageResponse> saveImage(MultipartFile image, String protection) {
        return ResponseEntity.ok(imageService.saveImage(image, protection));
    }
}
