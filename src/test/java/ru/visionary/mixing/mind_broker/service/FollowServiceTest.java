package ru.visionary.mixing.mind_broker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.repository.FollowRepository;
import ru.visionary.mixing.mind_broker.repository.UserRepository;
import ru.visionary.mixing.mind_broker.utils.SecurityContextUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FollowService followService;

    @Test
    void follow_ValidRequest_ShouldSave() {
        try (var utils = mockStatic(SecurityContextUtils.class)) {
            User currentUser = createTestUser(1L, true);
            User targetUser = createTestUser(2L, true);

            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(currentUser);
            when(userRepository.findById(2L)).thenReturn(targetUser);

            assertDoesNotThrow(() -> followService.follow(2L));
            verify(followRepository).save(any());
        }
    }

    @Test
    void follow_NonExistingUser_ShouldThrowException() {
        try (var utils = mockStatic(SecurityContextUtils.class)) {
            User currentUser = createTestUser(1L, true);

            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(currentUser);
            when(userRepository.findById(999L)).thenReturn(null);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> followService.follow(999L));
            assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        }
    }

    @Test
    void follow_SelfFollow_ShouldThrowException() {
        try (var utils = mockStatic(SecurityContextUtils.class)) {
            User currentUser = createTestUser(1L, true);

            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(currentUser);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> followService.follow(1L));
            assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        }
    }

    @Test
    void unfollow_ExistingFollow_ShouldDelete() {
        try (var utils = mockStatic(SecurityContextUtils.class)) {
            User currentUser = createTestUser(1L, true);

            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(currentUser);
            when(userRepository.findById(2L)).thenReturn(createTestUser(2L, true));
            when(followRepository.deleteByFollowerAndFollow(1L, 2L)).thenReturn(1);

            assertDoesNotThrow(() -> followService.unfollow(2L));
        }
    }

    @Test
    void unfollow_NotFollowing_ShouldThrowException() {
        try (var utils = mockStatic(SecurityContextUtils.class)) {
            User currentUser = createTestUser(1L, true);

            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(currentUser);
            when(userRepository.findById(2L)).thenReturn(createTestUser(2L, true));
            when(followRepository.deleteByFollowerAndFollow(1L, 2L)).thenReturn(0);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> followService.unfollow(2L));
            assertEquals(ErrorCode.NOT_FOLLOWING, ex.getErrorCode());
        }
    }

    @Test
    void follow_WhenFollowedUserIsInactive_ThrowsOwnerDeleted() {
        try (var utils = mockStatic(SecurityContextUtils.class)) {
            User activeUser = createTestUser(1L, true);
            User inactiveUser = createTestUser(2L, false);

            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(activeUser);
            when(userRepository.findById(2L)).thenReturn(inactiveUser);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> followService.follow(2L));
            assertEquals(ErrorCode.OWNER_DELETED, ex.getErrorCode());
        }
    }

    @Test
    void follow_WhenCurrentUserIsInactive_ThrowsCurrentUserDeleted() {
        try (var utils = mockStatic(SecurityContextUtils.class)) {
            User inactiveUser = createTestUser(1L, false);

            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(inactiveUser);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> followService.follow(2L));
            assertEquals(ErrorCode.CURRENT_USER_DELETED, ex.getErrorCode());
        }
    }

    @Test
    void follow_WhenUnauthenticated_ThrowsUserNotAuthorized() {
        try (var utils = mockStatic(SecurityContextUtils.class)) {
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(null);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> followService.follow(1L));
            assertEquals(ErrorCode.USER_NOT_AUTHORIZED, ex.getErrorCode());
        }
    }

    @Test
    void unfollow_WhenUserWasFollowingAndNowRefollow_ShouldWork() {
        try (var utils = mockStatic(SecurityContextUtils.class)) {
            User currentUser = createTestUser(1L, true);
            User targetUser = createTestUser(2L, true);

            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(currentUser);
            when(userRepository.findById(2L)).thenReturn(targetUser);

            followService.follow(2L);

            when(followRepository.deleteByFollowerAndFollow(1L, 2L)).thenReturn(1);
            followService.unfollow(2L);

            assertDoesNotThrow(() -> followService.follow(2L));
        }
    }

    @Test
    void follow_WhenAlreadyFollowingAfterPreviousUnfollow_ShouldSucceed() {
        try (var utils = mockStatic(SecurityContextUtils.class)) {
            User currentUser = createTestUser(1L, true);
            User targetUser = createTestUser(2L, true);

            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(currentUser);
            when(userRepository.findById(2L)).thenReturn(targetUser);

            followService.follow(2L);

            when(followRepository.deleteByFollowerAndFollow(1L, 2L)).thenReturn(1);
            followService.unfollow(2L);

            assertDoesNotThrow(() -> followService.follow(2L));
            verify(followRepository, times(2)).save(any());
        }
    }

    private User createTestUser(Long id, boolean active) {
        return User.builder()
                .id(id)
                .nickname("user" + id)
                .email("user" + id + "@example.com")
                .active(active)
                .build();
    }
}