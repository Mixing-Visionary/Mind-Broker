package ru.visionary.mixing.mind_broker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.visionary.mixing.generated.model.ImageResponse;
import ru.visionary.mixing.generated.model.SaveImageResponse;
import ru.visionary.mixing.generated.model.UpdateImageRequest;
import ru.visionary.mixing.mind_broker.entity.Image;
import ru.visionary.mixing.mind_broker.entity.Protection;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.repository.ImageRepository;
import ru.visionary.mixing.mind_broker.service.mapper.ImageMapper;
import ru.visionary.mixing.mind_broker.utils.SecurityContextUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {
    private final ImageRepository imageRepository;
    private final MinioService minioService;
    private final ImageMapper imageMapper;

    private static final Set<String> allowedContentType = Set.of("image/jpeg", "image/jpg");
    private static final Set<String> allowedExtension = Set.of("jpeg", "jpg");

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

        if (image.isEmpty()) {
            log.error("Uploading error: empty file");
            throw new ServiceException(ErrorCode.EMPTY_FILE);
        }

        if (!allowedContentType.contains(image.getContentType())
                || !allowedExtension.contains(FilenameUtils.getExtension(image.getOriginalFilename()))) {
            log.error("Uploading error: not supported file format");
            throw new ServiceException(ErrorCode.FILE_FORMAT_NOT_SUPPORTED);
        }

        BufferedImage img;
        try {
            img = ImageIO.read(image.getInputStream());
        } catch (Exception e) {
            log.error("Uploading error: not supported file format");
            throw new ServiceException(ErrorCode.FILE_FORMAT_NOT_SUPPORTED);
        }

        if (img == null) {
            log.error("Uploading error: not supported file format");
            throw new ServiceException(ErrorCode.FILE_FORMAT_NOT_SUPPORTED);
        }

        User user = SecurityContextUtils.getAuthenticatedUser();
        if (user == null) {
            log.error("Uploading error: user not authorized");
            throw new ServiceException(ErrorCode.USER_NOT_AUTHORIZED);
        }

        log.debug("Processing image metadata. User: {}", user.getEmail());

        LocalDateTime now = LocalDateTime.now();
        UUID uuid = imageRepository.save(new Image()
                .setOwner(user)
                .setProtection(imageProtection)
                .setCreatedAt(now));

        log.debug("Uploading to MinIO. UUID: {}", uuid);
        minioService.uploadFile(image, uuid);

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

        if (Protection.PRIVATE.equals(image.getProtection()) &&
                (user == null || !(user.getId().equals(image.getOwner().getId()) || BooleanUtils.isTrue(user.getAdmin())))) {
            log.error("Fetching error: use doesn't have access");
            throw new ServiceException(ErrorCode.ACCESS_FORBIDEN);
        }

        log.info("Successfully retrieved image: {}", uuid);
        return imageMapper.toResponse(imageRepository.findById(uuid));
    }

    public void updateImage(UUID uuid, UpdateImageRequest request) {
        log.info("Updating protection for image {} to {}", uuid, request.getProtection());
        User user = SecurityContextUtils.getAuthenticatedUser();
        Image image = imageRepository.findById(uuid);

        if (image.getId() == null) {
            log.error("Updating error: image not found");
            throw new ServiceException(ErrorCode.IMAGE_NOT_FOUND);
        }

        if (user == null || !(user.getId().equals(image.getOwner().getId()) || BooleanUtils.isTrue(user.getAdmin()))) {
            log.error("Fetching error: use doesn't have access");
            throw new ServiceException(ErrorCode.ACCESS_FORBIDEN);
        }

        imageRepository.updateProtection(uuid, Protection.valueOf(request.getProtection().getValue().toUpperCase()));

        log.info("Protection updated successfully");
    }

    @Transactional
    public void deleteById(UUID uuid) {
        log.info("Starting deleting image {}", uuid);

        User user = SecurityContextUtils.getAuthenticatedUser();
        Image image = imageRepository.findById(uuid);

        if (image.getId() == null) {
            log.error("Updating error: image not found");
            throw new ServiceException(ErrorCode.IMAGE_NOT_FOUND);
        }

        if (user == null || !(user.getId().equals(image.getOwner().getId()) || BooleanUtils.isTrue(user.getAdmin()))) {
            log.error("Fetching error: use doesn't have access");
            throw new ServiceException(ErrorCode.ACCESS_FORBIDEN);
        }

        log.info("Deleting image {}", uuid);

        imageRepository.deleteById(uuid);
        minioService.deleteFile(uuid);

        log.info("Image deleted successfully");
    }
}
