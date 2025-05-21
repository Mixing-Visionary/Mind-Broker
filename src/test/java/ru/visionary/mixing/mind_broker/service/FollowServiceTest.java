package ru.visionary.mixing.mind_broker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.visionary.mixing.generated.model.UsersResponse;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.repository.FollowRepository;
import ru.visionary.mixing.mind_broker.repository.UserRepository;
import ru.visionary.mixing.mind_broker.service.mapper.UserMapperImpl;
import ru.visionary.mixing.mind_broker.utils.SecurityContextUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {
    @Mock
    private FollowRepository followRepository;
    @Mock
    private UserRepository userRepository;
    @Spy
    private UserMapperImpl userMapper;

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

    @Test
    void getCurrentFollowers_AuthenticatedUser_ReturnsFollowers() {
        try(var utils = mockStatic(SecurityContextUtils.class)) {
            User currentUser = createTestUser(1L, true);
            List<User> followers = List.of(createTestUser(2L, true));
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(currentUser);

            when(followRepository.getFollowers(currentUser.id(), 10, 0))
                    .thenReturn(followers);

            UsersResponse response = followService.getCurrentFollowers(10, 0);

            assertEquals(1, response.getUsers().size());
            verify(followRepository).getFollowers(1L, 10, 0);
        }
    }

    @Test
    void getCurrentFollowers_Unauthenticated_ThrowsException() {
        try(var utils = mockStatic(SecurityContextUtils.class)) {
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(null);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> followService.getCurrentFollowers(10, 0));

            assertEquals(ErrorCode.USER_NOT_AUTHORIZED, ex.getErrorCode());
        }
    }

    @Test
    void getCurrentFollowers_InactiveUser_ThrowsException() {
        try(var utils = mockStatic(SecurityContextUtils.class)) {
            User inactiveUser = createTestUser(1L, false);
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(inactiveUser);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> followService.getCurrentFollowers(10, 0));

            assertEquals(ErrorCode.CURRENT_USER_DELETED, ex.getErrorCode());
        }
    }

    @Test
    void getCurrentFollows_ValidRequest_ReturnsFollows() {
        try(var utils = mockStatic(SecurityContextUtils.class)) {
            User currentUser = createTestUser(1L, true);
            List<User> follows = List.of(createTestUser(3L, true));
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(currentUser);

            when(followRepository.getFollows(currentUser.id(), 10, 0))
                    .thenReturn(follows);

            UsersResponse response = followService.getCurrentFollows(10, 0);

            assertEquals(1, response.getUsers().size());
            verify(followRepository).getFollows(1L, 10, 0);
        }
    }

    @Test
    void getFollowers_ValidUserId_ReturnsFollowers() {
        Long userId = 2L;
        User targetUser = createTestUser(userId, true);
        List<User> followers = List.of(createTestUser(3L, true));

        when(userRepository.findById(userId)).thenReturn(targetUser);
        when(followRepository.getFollowers(userId, 10, 0)).thenReturn(followers);

        UsersResponse response = followService.getFollowers(userId, 10, 0);

        assertEquals(1, response.getUsers().size());
        verify(userRepository).findById(userId);
        verify(followRepository).getFollowers(userId, 10, 0);
    }

    @Test
    void getFollowers_UserNotFound_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> followService.getFollowers(999L, 10, 0));

        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getFollowers_InactiveUser_ThrowsException() {
        User inactiveUser = createTestUser(2L, false);
        when(userRepository.findById(2L)).thenReturn(inactiveUser);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> followService.getFollowers(2L, 10, 0));

        assertEquals(ErrorCode.OWNER_DELETED, ex.getErrorCode());
    }

    @Test
    void getFollows_ValidRequest_ReturnsFollows() {
        Long userId = 2L;
        User targetUser = createTestUser(userId, true);
        List<User> follows = List.of(createTestUser(4L, true));

        when(userRepository.findById(userId)).thenReturn(targetUser);
        when(followRepository.getFollows(userId, 10, 0)).thenReturn(follows);

        UsersResponse response = followService.getFollows(userId, 10, 0);

        assertEquals(1, response.getUsers().size());
        verify(followRepository).getFollows(userId, 10, 0);
    }

    @Test
    void getFollows_PaginationSecondPage_ReturnsLimitedResults() {
        Long userId = 2L;
        User targetUser = createTestUser(userId, true);

        when(userRepository.findById(userId)).thenReturn(targetUser);
        when(followRepository.getFollows(userId, 20, 1)).thenReturn(Collections.emptyList());

        UsersResponse response = followService.getFollows(userId, 20, 1);

        assertTrue(response.getUsers().isEmpty());
        verify(followRepository).getFollows(userId, 20, 1);
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