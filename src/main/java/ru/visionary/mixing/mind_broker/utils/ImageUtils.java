package ru.visionary.mixing.mind_broker.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Set;

@Slf4j
public class ImageUtils {
    private static final Set<String> allowedContentType = Set.of("image/jpeg", "image/jpg", "image/png");
    private static final Set<String> allowedExtension = Set.of("jpeg", "jpg", "png");

    public static void checkImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            log.error("Image error: empty file");
            throw new ServiceException(ErrorCode.EMPTY_FILE);
        }

        if (!allowedContentType.contains(image.getContentType())
                || !allowedExtension.contains(FilenameUtils.getExtension(image.getOriginalFilename()))) {
            log.error("Image error: not supported file format");
            throw new ServiceException(ErrorCode.FILE_FORMAT_NOT_SUPPORTED);
        }

        BufferedImage img;
        try (InputStream is = image.getInputStream()) {
            img = ImageIO.read(is);
        } catch (Exception e) {
            log.error("Image error: not supported file format");
            throw new ServiceException(ErrorCode.FILE_FORMAT_NOT_SUPPORTED);
        }

        if (img == null) {
            log.error("Image error: not supported file format");
            throw new ServiceException(ErrorCode.FILE_FORMAT_NOT_SUPPORTED);
        }
    }
}
