package ru.visionary.mixing.mind_broker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.visionary.mixing.generated.api.ImageApi;
import ru.visionary.mixing.generated.model.ImageResponse;
import ru.visionary.mixing.generated.model.SaveImageResponse;
import ru.visionary.mixing.generated.model.UpdateImageRequest;
import ru.visionary.mixing.mind_broker.service.ImageService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ImageController implements ImageApi {
    private final ImageService imageService;

    @Override
    public ResponseEntity<SaveImageResponse> saveImage(MultipartFile image, String protection) {
        return ResponseEntity.ok(imageService.saveImage(image, protection));
    }

    @Override
    public ResponseEntity<ImageResponse> getImage(UUID uuid) {
        return ResponseEntity.ok(imageService.getImage(uuid));
    }

    @Override
    public ResponseEntity<Void> updateImage(UUID uuid, UpdateImageRequest updateImageRequest) {
        imageService.updateImage(uuid, updateImageRequest);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteImage(UUID uuid) {
        imageService.deleteById(uuid);
        return ResponseEntity.ok().build();
    }
}
