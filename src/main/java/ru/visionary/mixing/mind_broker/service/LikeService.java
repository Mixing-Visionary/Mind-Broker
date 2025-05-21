package ru.visionary.mixing.mind_broker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.visionary.mixing.mind_broker.entity.Image;
import ru.visionary.mixing.mind_broker.entity.Protection;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.repository.ImageRepository;
import ru.visionary.mixing.mind_broker.repository.LikeRepository;
import ru.visionary.mixing.mind_broker.utils.SecurityContextUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {
    private final ImageRepository imageRepository;
    private final LikeRepository likeRepository;

    public void likeImage(UUID imageUuid) {
        User user = SecurityContextUtils.getAuthenticatedUser();
        if (user == null) {
            log.warn("Like attempt by unauthorized user for image: {}", imageUuid);
            throw new ServiceException(ErrorCode.USER_NOT_AUTHORIZED);
        }
        if (!user.active()) {
            log.warn("Like attempt by deleted user {} for image: {}", user.id(), imageUuid);
            throw new ServiceException(ErrorCode.CURRENT_USER_DELETED);
        }

        Image image = imageRepository.findById(imageUuid);
        if (image == null) {
            log.error("Like failed - image not found: {}", imageUuid);
            throw new ServiceException(ErrorCode.IMAGE_NOT_FOUND);
        }
        if (Protection.PRIVATE.equals(image.protection())) {
            log.error("Like attempt to private image: {}", imageUuid);
            throw new ServiceException(ErrorCode.ACCESS_FORBIDDEN);
        }
        if (!image.owner().active()) {
            log.error("Like attempt to deleted user's image: {}", imageUuid);
            throw new ServiceException(ErrorCode.OWNER_DELETED);
        }

        likeRepository.save(user.id(), imageUuid);

        log.info("Successfully liked image {} by user {}", imageUuid, user.id());
    }

    public void dislikeImage(UUID imageUuid) {
        User user = SecurityContextUtils.getAuthenticatedUser();
        if (user == null) {
            log.warn("Dislike attempt by unauthorized user for image: {}", imageUuid);
            throw new ServiceException(ErrorCode.USER_NOT_AUTHORIZED);
        }
        if (!user.active()) {
            log.warn("Dislike attempt by deleted user {} for image: {}", user.id(), imageUuid);
            throw new ServiceException(ErrorCode.CURRENT_USER_DELETED);
        }

        log.info("User {} attempting to dislike image {}", user.id(), imageUuid);

        Image image = imageRepository.findById(imageUuid);
        if (image == null) {
            log.error("Dislike failed - image not found: {}", imageUuid);
            throw new ServiceException(ErrorCode.IMAGE_NOT_FOUND);
        }
        if (Protection.PRIVATE.equals(image.protection())) {
            log.error("Dislike attempt to private image: {}", imageUuid);
            throw new ServiceException(ErrorCode.ACCESS_FORBIDDEN);
        }
        if (!image.owner().active()) {
            log.error("Dislike attempt to deleted user's image: {}", imageUuid);
            throw new ServiceException(ErrorCode.OWNER_DELETED);
        }

        int affectedRows = likeRepository.deleteByUserAndImage(user.id(), imageUuid);
        if (affectedRows == 0) {
            log.warn("Dislike failed - no existing like for user {} on image {}", user.id(), imageUuid);
            throw new ServiceException(ErrorCode.NOT_LIKED);
        }

        log.info("Successfully disliked image {} by user {}", imageUuid, user.id());
    }
}
