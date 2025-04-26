package ru.visionary.mixing.mind_broker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.visionary.mixing.generated.model.SaveImageResponse;
import ru.visionary.mixing.mind_broker.entity.Image;
import ru.visionary.mixing.mind_broker.entity.Protection;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.repository.ImageRepository;
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

    private static final Set<String> allowedContentType = Set.of("image/jpeg", "image/jpg");
    private static final Set<String> allowedExtension = Set.of("jpeg", "jpg");

    @Transactional
    public SaveImageResponse saveImage(MultipartFile image, String protection) {
        Protection imageProtection;
        try {
            imageProtection = Protection.valueOf(protection.toUpperCase());
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.INVALID_REQUEST);
        }

        if (image.isEmpty()) {
            throw new ServiceException(ErrorCode.EMPTY_FILE);
        }

        if (!allowedContentType.contains(image.getContentType())
                || StringUtils.isBlank(image.getOriginalFilename())
                || !allowedExtension.contains(getFileExtension(image.getOriginalFilename()))) {
            throw new ServiceException(ErrorCode.FILE_FORMAT_NOT_SUPPORTED);
        }

        BufferedImage img;
        try {
            img = ImageIO.read(image.getInputStream());
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.FILE_FORMAT_NOT_SUPPORTED);
        }

        if (img == null) {
            throw new ServiceException(ErrorCode.FILE_FORMAT_NOT_SUPPORTED);
        }

        User user = SecurityContextUtils.getAuthenticatedUser();
        if (user == null) {
            throw new ServiceException(ErrorCode.USER_NOT_AUTHORIZED);
        }

        LocalDateTime now = LocalDateTime.now();

        UUID uuid = imageRepository.save(new Image()
                .setOwner(user)
                .setProtection(imageProtection)
                .setCreatedAt(now));
        minioService.uploadFile(image, uuid);

        return new SaveImageResponse(uuid.toString());
    }

    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }
}
