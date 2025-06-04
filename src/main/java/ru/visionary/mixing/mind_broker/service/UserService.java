package ru.visionary.mixing.mind_broker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.visionary.mixing.generated.model.UserResponse;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.repository.LikeRepository;
import ru.visionary.mixing.mind_broker.repository.RefreshTokenRepository;
import ru.visionary.mixing.mind_broker.repository.UserRepository;
import ru.visionary.mixing.mind_broker.service.mapper.UserMapper;
import ru.visionary.mixing.mind_broker.utils.ImageUtils;
import ru.visionary.mixing.mind_broker.utils.SecurityContextUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LikeRepository likeRepository;
    private final MinioService minioService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserResponse getUser(long userId) {
        log.info("Fetching user info for id: {}", userId);

        User user = userRepository.findById(userId);

        if (user == null) {
            log.error("Fetching error: user not found");
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }
        if (!user.active()) {
            log.error("Fetching error: user deleted");
            throw new ServiceException(ErrorCode.OWNER_DELETED);
        }

        long likes = likeRepository.getUserLikesCount(userId);

        log.info("Successfully retrieved user: {}", userId);
        return userMapper.toResponse(user).likes(likes);
    }

    public UserResponse getCurrentUser() {
        log.info("Fetching current user");
        User user = SecurityContextUtils.getAuthenticatedUser();

        if (user == null) {
            log.error("Fetching error: user not authorized");
            throw new ServiceException(ErrorCode.USER_NOT_AUTHORIZED);
        }
        if (!user.active()) {
            log.error("Fetching error: user deleted");
            throw new ServiceException(ErrorCode.CURRENT_USER_DELETED);
        }

        long likes = likeRepository.getUserLikesCount(user.id());

        log.info("Successfully retrieved user");
        return userMapper.toResponse(user).likes(likes);
    }

    @Transactional
    public void updateUser(long userId, String nickname, String description, String password, MultipartFile avatar) {
        log.info("Starting user update for ID: {}", userId);
        User authorizedUser = SecurityContextUtils.getAuthenticatedUser();
        if (authorizedUser == null) {
            log.warn("Update attempt without authorization");
            throw new ServiceException(ErrorCode.USER_NOT_AUTHORIZED);
        }

        log.debug("Authorization check - User ID: {}, Is Admin: {}", authorizedUser.id(), authorizedUser.admin());

        if (!authorizedUser.active()) {
            log.error("Attempt to update by inactive user: {}", authorizedUser.email());
            throw new ServiceException(ErrorCode.CURRENT_USER_DELETED);
        }
        if (!(authorizedUser.id().equals(userId) || authorizedUser.admin())) {
            log.warn("Unauthorized update attempt. User {} trying to update {}", authorizedUser.id(), userId);
            throw new ServiceException(ErrorCode.ACCESS_FORBIDDEN);
        }

        User updatingUser = authorizedUser.id().equals(userId) ? authorizedUser : userRepository.findById(userId);
        if (updatingUser == null) {
            log.error("User not found for update: {}", userId);
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }
        if (!updatingUser.active()) {
            log.error("Attempt to update inactive user: {}", updatingUser.email());
            throw new ServiceException(ErrorCode.OWNER_DELETED);
        }

        log.debug("Updating user: {}", updatingUser.email());

        password = password == null ? null : passwordEncoder.encode(password);
        UUID avatarUuid = null;
        if (avatar != null) {
            log.debug("Uploading new avatar for user: {}", userId);
            ImageUtils.checkImage(avatar);
            avatarUuid = UUID.randomUUID();
        }

        userRepository.updateUser(userId, nickname, description, password, avatarUuid);

        if (password != null) {
            log.info("Password changed, deleting refresh tokens for user: {}", userId);
            refreshTokenRepository.deleteByUser(userId);
        }

        if (avatarUuid != null) {
            log.debug("Uploading new avatar for user: {}", userId);
            minioService.uploadAvatar(avatar, avatarUuid);
            if (updatingUser.avatar() != null) {
                minioService.deleteAvatar(updatingUser.avatar());
            }
        }

        log.info("User {} updated successfully", userId);
    }

    @Transactional
    public void updateCurrentUser(String nickname, String description, String password, MultipartFile avatar) {
        log.info("Starting update current user");

        User user = SecurityContextUtils.getAuthenticatedUser();
        if (user == null) {
            log.warn("Update attempt without authorization");
            throw new ServiceException(ErrorCode.USER_NOT_AUTHORIZED);
        }

        if (!user.active()) {
            log.error("Attempt to update by inactive user: {}", user.email());
            throw new ServiceException(ErrorCode.CURRENT_USER_DELETED);
        }

        log.debug("Updating user: {}", user.email());

        password = password == null ? null : passwordEncoder.encode(password);
        UUID avatarUuid = null;
        if (avatar != null) {
            log.debug("Uploading new avatar for user: {}", user.id());
            ImageUtils.checkImage(avatar);
            avatarUuid = UUID.randomUUID();
        }

        userRepository.updateUser(user.id(), nickname, description, password, avatarUuid);

        if (password != null) {
            log.info("Password changed, deleting refresh tokens for user: {}", user.id());
            refreshTokenRepository.deleteByUser(user.id());
        }

        if (avatarUuid != null) {
            log.debug("Uploading new avatar for user: {}", user.id());
            minioService.uploadAvatar(avatar, avatarUuid);
            if (user.avatar() != null) {
                minioService.deleteAvatar(user.avatar());
            }
        }

        log.info("User {} updated successfully", user.id());
    }

    @Transactional
    public void deleteUser(long userId) {
        log.info("Starting user deletion for ID: {}", userId);

        User authorizedUser = SecurityContextUtils.getAuthenticatedUser();
        if (authorizedUser == null) {
            log.warn("Delete attempt without authorization");
            throw new ServiceException(ErrorCode.USER_NOT_AUTHORIZED);
        }
        if (!authorizedUser.active()) {
            log.error("Delete attempt by inactive user: {}", authorizedUser.email());
            throw new ServiceException(ErrorCode.CURRENT_USER_DELETED);
        }
        if (!(authorizedUser.id().equals(userId) || authorizedUser.admin())) {
            log.warn("Unauthorized delete attempt. User {} trying to delete {}", authorizedUser.id(), userId);
            throw new ServiceException(ErrorCode.ACCESS_FORBIDDEN);
        }

        User user = userRepository.findById(userId);
        if (user == null) {
            log.error("User not found for deletion: {}", userId);
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }

        userRepository.deleteUser(userId);
        refreshTokenRepository.deleteByUser(userId);

        log.info("User {} deleted successfully", userId);
    }

    @Transactional
    public void deleteCurrentUser() {
        log.info("Starting deletion current user");

        User user = SecurityContextUtils.getAuthenticatedUser();
        if (user == null) {
            log.warn("Delete attempt without authorization");
            throw new ServiceException(ErrorCode.USER_NOT_AUTHORIZED);
        }
        if (!user.active()) {
            log.error("Delete attempt by inactive user: {}", user.email());
            throw new ServiceException(ErrorCode.CURRENT_USER_DELETED);
        }

        userRepository.deleteUser(user.id());
        refreshTokenRepository.deleteByUser(user.id());

        log.info("User {} deleted successfully", user.id());
    }

    @Transactional
    public void deleteAvatar(long userId) {
        log.info("Starting avatar deletion for user: {}", userId);

        User authorizedUser = SecurityContextUtils.getAuthenticatedUser();
        if (authorizedUser == null) {
            log.warn("Avatar deletion attempt without authorization");
            throw new ServiceException(ErrorCode.USER_NOT_AUTHORIZED);
        }
        if (!authorizedUser.active()) {
            log.error("Avatar deletion attempt by inactive user: {}", authorizedUser.email());
            throw new ServiceException(ErrorCode.CURRENT_USER_DELETED);
        }
        if (!(authorizedUser.id().equals(userId) || authorizedUser.admin())) {
            log.warn("Unauthorized avatar deletion. User {} trying to delete avatar for {}", authorizedUser.id(), userId);
            throw new ServiceException(ErrorCode.ACCESS_FORBIDDEN);
        }

        User user = userRepository.findById(userId);
        if (user == null) {
            log.error("User not found for avatar deletion: {}", userId);
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }
        if (!user.active()) {
            log.error("Avatar deletion for inactive user: {}", userId);
            throw new ServiceException(ErrorCode.OWNER_DELETED);
        }

        UUID avatarUuid = user.avatar();
        if (avatarUuid != null) {
            log.debug("Deleting avatar file: {}", avatarUuid);
            userRepository.deleteAvatar(userId);
            minioService.deleteAvatar(avatarUuid);
            log.info("Avatar deleted for user: {}", userId);
        }
    }

    @Transactional
    public void deleteCurrentAvatar() {
        log.info("Starting avatar deletion for current user");

        User user = SecurityContextUtils.getAuthenticatedUser();
        if (user == null) {
            log.warn("Avatar deletion attempt without authorization");
            throw new ServiceException(ErrorCode.USER_NOT_AUTHORIZED);
        }
        if (!user.active()) {
            log.error("Avatar deletion attempt by inactive user: {}", user.email());
            throw new ServiceException(ErrorCode.CURRENT_USER_DELETED);
        }

        UUID avatarUuid = user.avatar();
        if (avatarUuid != null) {
            log.debug("Deleting avatar file: {}", avatarUuid);
            userRepository.deleteAvatar(user.id());
            minioService.deleteAvatar(avatarUuid);
            log.info("Avatar deleted for user: {}", user.id());
        }
    }
}
