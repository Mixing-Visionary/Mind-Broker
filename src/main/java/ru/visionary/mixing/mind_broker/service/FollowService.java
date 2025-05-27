package ru.visionary.mixing.mind_broker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.visionary.mixing.generated.model.UsersResponse;
import ru.visionary.mixing.mind_broker.entity.Follow;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.repository.FollowRepository;
import ru.visionary.mixing.mind_broker.repository.UserRepository;
import ru.visionary.mixing.mind_broker.service.mapper.UserMapper;
import ru.visionary.mixing.mind_broker.utils.SecurityContextUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public void follow(Long userId) {
        User currentUser = SecurityContextUtils.getAuthenticatedUser();

        if (currentUser == null) {
            log.warn("Unauthorized follow attempt");
            throw new ServiceException(ErrorCode.USER_NOT_AUTHORIZED);
        }
        if (!currentUser.active()) {
            log.warn("Follow attempt by deleted user: {}", currentUser.id());
            throw new ServiceException(ErrorCode.CURRENT_USER_DELETED);
        }

        log.info("Follow attempt - Follower: {}, Target: {}", currentUser.id(), userId);

        if (currentUser.id().equals(userId)) {
            log.warn("Self-follow attempt - User: {}", userId);
            throw new ServiceException(ErrorCode.INVALID_REQUEST);
        }

        User followUser = userRepository.findById(userId);

        if (followUser == null) {
            log.error("Follow failed - Target user not found: {}", userId);
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }
        if (!followUser.active()) {
            log.warn("Follow failed - Target user deleted: {}", userId);
            throw new ServiceException(ErrorCode.OWNER_DELETED);
        }

        Follow newFollow = Follow.builder()
                .follower(currentUser)
                .follow(followUser)
                .build();

        followRepository.save(newFollow);
        log.info("Follow success - {} started following {}", currentUser.id(), userId);
    }

    public void unfollow(Long userId) {
        User currentUser = SecurityContextUtils.getAuthenticatedUser();

        if (currentUser == null) {
            log.warn("Unauthorized unfollow attempt");
            throw new ServiceException(ErrorCode.USER_NOT_AUTHORIZED);
        }
        if (!currentUser.active()) {
            log.warn("Unfollow attempt by deleted user: {}", currentUser.id());
            throw new ServiceException(ErrorCode.CURRENT_USER_DELETED);
        }

        log.info("Unfollow attempt - Follower: {}, Target: {}", currentUser.id(), userId);

        if (currentUser.id().equals(userId)) {
            log.warn("Self-unfollow attempt - User: {}", userId);
            throw new ServiceException(ErrorCode.INVALID_REQUEST);
        }

        if (userRepository.findById(userId) == null) {
            log.error("Unfollow failed - Target user not found: {}", userId);
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }

        int affectedRows = followRepository.deleteByFollowerAndFollow(currentUser.id(), userId);
        if (affectedRows == 0) {
            log.warn("Unfollow conflict - Not following: {} -> {}", currentUser.id(), userId);
            throw new ServiceException(ErrorCode.NOT_FOLLOWING);
        }

        log.info("Unfollow success - {} stopped following {}", currentUser.id(), userId);
    }

    public UsersResponse getCurrentFollowers(Integer size, Integer page) {
        User user = SecurityContextUtils.getAuthenticatedUser();

        if (user == null) {
            throw new ServiceException(ErrorCode.USER_NOT_AUTHORIZED);
        }
        if (!user.active()) {
            throw new ServiceException(ErrorCode.CURRENT_USER_DELETED);
        }

        List<User> followers = followRepository.getFollowers(user.id(), size, page);

        return new UsersResponse().users(userMapper.toResponse(followers));
    }

    public UsersResponse getCurrentFollows(Integer size, Integer page) {
        User user = SecurityContextUtils.getAuthenticatedUser();

        if (user == null) {
            throw new ServiceException(ErrorCode.USER_NOT_AUTHORIZED);
        }
        if (!user.active()) {
            throw new ServiceException(ErrorCode.CURRENT_USER_DELETED);
        }

        List<User> follows = followRepository.getFollows(user.id(), size, page);

        return new UsersResponse().users(userMapper.toResponse(follows));
    }

    public UsersResponse getFollowers(Long userId, Integer size, Integer page) {
        User user = userRepository.findById(userId);

        if (user == null) {
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }
        if (!user.active()) {
            throw new ServiceException(ErrorCode.OWNER_DELETED);
        }

        List<User> followers = followRepository.getFollowers(user.id(), size, page);

        return new UsersResponse().users(userMapper.toResponse(followers));
    }

    public UsersResponse getFollows(Long userId, Integer size, Integer page) {
        User user = userRepository.findById(userId);

        if (user == null) {
            throw new ServiceException(ErrorCode.USER_NOT_FOUND);
        }
        if (!user.active()) {
            throw new ServiceException(ErrorCode.OWNER_DELETED);
        }

        List<User> follows = followRepository.getFollows(user.id(), size, page);

        return new UsersResponse().users(userMapper.toResponse(follows));
    }
}
