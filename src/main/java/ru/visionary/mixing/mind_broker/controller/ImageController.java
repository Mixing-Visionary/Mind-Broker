package ru.visionary.mixing.mind_broker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.visionary.mixing.generated.api.ImageApi;
import ru.visionary.mixing.generated.model.GetImagesResponse;
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
    public ResponseEntity<SaveImageResponse> saveImage(String protection, MultipartFile image) {
        return ResponseEntity.ok(imageService.saveImage(image, protection));
    }

    @Override
    public ResponseEntity<GetImagesResponse> getCurrentUserImages(Integer size, Integer page, String protection) {
        return ResponseEntity.ok(imageService.getImagesForCurrentUser(size, page, protection));
    }

    @Override
    public ResponseEntity<GetImagesResponse> getUserImages(Long userId, Integer size, Integer page) {
        return ResponseEntity.ok(imageService.getImagesByUserId(userId, size, page));
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
