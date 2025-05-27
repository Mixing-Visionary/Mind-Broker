package ru.visionary.mixing.mind_broker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.visionary.mixing.generated.model.GetImagesResponse;
import ru.visionary.mixing.generated.model.ImageResponse;
import ru.visionary.mixing.generated.model.SaveImageResponse;
import ru.visionary.mixing.generated.model.UpdateImageRequest;
import ru.visionary.mixing.mind_broker.entity.Image;
import ru.visionary.mixing.mind_broker.entity.Protection;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.repository.ImageRepository;
import ru.visionary.mixing.mind_broker.repository.UserRepository;
import ru.visionary.mixing.mind_broker.service.mapper.ImageMapper;
import ru.visionary.mixing.mind_broker.utils.ImageUtils;
import ru.visionary.mixing.mind_broker.utils.SecurityContextUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final MinioService minioService;
    private final ImageMapper imageMapper;

    @Transactional
    public SaveImageResponse saveImage(MultipartFile image, String protection) {
        log.info("Image upload started. Size: {} bytes, Type: {}", image.getSize(), image.getContentType());

        Protection imageProtection;
        try {
            imageProtection = Protection.valueOf(protection.toUpperCase());
        } catch (Exception e) {
            log.error("Uploading error: protection invalid");
            throw new ServiceException(ErrorCode.INVALID_REQUEST);
        }

        ImageUtils.checkImage(image);

        User user = SecurityContextUtils.getAuthenticatedUser();
        if (user == null) {
            log.error("Uploading error: user not authorized");
            throw new ServiceException(ErrorCode.USER_NOT_AUTHORIZED);
        }
        if (!user.active()) {
            log.error("Uploading error: user is inactive");
            throw new ServiceException(ErrorCode.CURRENT_USER_DELETED);
        }

        log.debug("Processing image metadata. User: {}", user.email());

        LocalDateTime now = LocalDateTime.now();
        UUID uuid = imageRepository.save(Image.builder()
                .owner(user)
                .protection(imageProtection)
                .createdAt(now)
                .build());

        log.debug("Uploading to MinIO. UUID: {}", uuid);
        minioService.uploadImage(image, uuid);

        log.info("Image successfully saved. UUID: {}, Protection: {}", uuid, imageProtection);

        return new SaveImageResponse(uuid);
    }

    public ImageResponse getImage(UUID uuid) {
        log.info("Fetching image metadata for UUID: {}", uuid);

        User user = SecurityContextUtils.getAuthenticatedUser();
        Image image = imageRepository.findById(uuid);

        if (image == null) {
            log.error("Fetching error: image not found");
            throw new ServiceException(ErrorCode.IMAGE_NOT_FOUND);
        }

        if (!image.owner().active()) {
            log.error("Fetching error: owner is deleted");
            throw new ServiceException(ErrorCode.OWNER_DELETED);
        }
        if (user != null && !user.active()) {
            log.error("Fetching error: user is deleted");
            throw new ServiceException(ErrorCode.CURRENT_USER_DELETED);
        }

        if (Protection.PRIVATE.equals(image.protection()) &&
                (user == null ||
                        !(user.id().equals(image.owner().id()) || BooleanUtils.isTrue(user.admin())))) {
            log.error("Fetching error: user doesn't have access");
            throw new ServiceException(ErrorCode.ACCESS_FORBIDDEN);
        }

        log.info("Successfully retrieved image: {}", uuid);
        return imageMapper.toResponse(imageRepository.findById(uuid));
    }

    public GetImagesResponse getImagesForCurrentUser(int size, int page, String protection) {
        log.info("Fetching images for current user: size={}, page={}, protection={}", size, page, protection);
        User user = SecurityContextUtils.getAuthenticatedUser();

        if (user == null) {
            log.info("Fetching error: user not authorized");
            throw new ServiceException(ErrorCode.USER_NOT_AUTHORIZED);
        }
        if (!user.active()) {
            log.info("Fetching error: user is deleted");
            throw new ServiceException(ErrorCode.CURRENT_USER_DELETED);
        }

        Protection imageProtection;
        try {
            imageProtection = Protection.valueOf(protection.toUpperCase());
        } catch (Exception e) {
            log.error("Uploading error: protection invalid");
            throw new ServiceException(ErrorCode.INVALID_REQUEST);
        }

        List<Image> images = imageRepository.findByOwnerAndProtection(user.id(), imageProtection, size, page);

        log.info("Found {} images for user: {}", images.size(), user.email());

        return new GetImagesResponse(imageMapper.toResponse(images));
    }

    public GetImagesResponse getImagesByUserId(long userId, int size, int page) {
        log.info("Fetching images for user {}: size={}, page={}", userId, size, page);

        User user = userRepository.findById(userId);

        if (user == null) {
            log.info("Fetching error: user not authorized");
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }
        if (!user.active()) {
            log.info("Fetching error: user is deleted");
            throw new ServiceException(ErrorCode.OWNER_DELETED);
        }

        List<Image> images = imageRepository.findByOwnerAndProtection(userId, Protection.PUBLIC, size, page);

        log.info("Found {} public images for user {}", images.size(), userId);

        return new GetImagesResponse(imageMapper.toResponse(images));
    }

    public void updateImage(UUID uuid, UpdateImageRequest request) {
        log.info("Updating protection for image {} to {}", uuid, request.getProtection());
        User user = SecurityContextUtils.getAuthenticatedUser();
        Image image = imageRepository.findById(uuid);

        if (image == null) {
            log.error("Updating error: image not found");
            throw new ServiceException(ErrorCode.IMAGE_NOT_FOUND);
        }

        if (!image.owner().active()) {
            log.error("Updating error: owner is deleted");
            throw new ServiceException(ErrorCode.OWNER_DELETED);
        }
        if (user != null && !user.active()) {
            log.error("Updating error: user is deleted");
            throw new ServiceException(ErrorCode.CURRENT_USER_DELETED);
        }

        if (user == null ||
                !(user.id().equals(image.owner().id()) || BooleanUtils.isTrue(user.admin()))) {
            log.error("Updating error: user doesn't have access");
            throw new ServiceException(ErrorCode.ACCESS_FORBIDDEN);
        }

        imageRepository.updateProtection(uuid, Protection.valueOf(request.getProtection().getValue().toUpperCase()));

        log.info("Protection updated successfully");
    }

    @Transactional
    public void deleteById(UUID uuid) {
        log.info("Starting deleting image {}", uuid);

        User user = SecurityContextUtils.getAuthenticatedUser();
        Image image = imageRepository.findById(uuid);

        if (image == null) {
            log.error("Deleting error: image not found");
            throw new ServiceException(ErrorCode.IMAGE_NOT_FOUND);
        }

        if (!image.owner().active()) {
            log.error("Deleting error: owner is deleted");
            throw new ServiceException(ErrorCode.OWNER_DELETED);
        }
        if (user != null && !user.active()) {
            log.error("Deleting error: user is deleted");
            throw new ServiceException(ErrorCode.CURRENT_USER_DELETED);
        }

        if (user == null
                || !(user.id().equals(image.owner().id()) || BooleanUtils.isTrue(user.admin()))) {
            log.error("Deleting error: user doesn't have access");
            throw new ServiceException(ErrorCode.ACCESS_FORBIDDEN);
        }

        log.info("Deleting image {}", uuid);

        imageRepository.deleteById(uuid);
        minioService.deleteImage(uuid);

        log.info("Image deleted successfully");
    }
}
