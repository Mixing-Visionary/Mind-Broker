package ru.visionary.mixing.mind_broker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.visionary.mixing.generated.model.UserResponse;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.repository.RefreshTokenRepository;
import ru.visionary.mixing.mind_broker.repository.UserRepository;
import ru.visionary.mixing.mind_broker.service.mapper.UserMapperImpl;
import ru.visionary.mixing.mind_broker.utils.SecurityContextUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private MinioService minioService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Spy
    private UserMapperImpl userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void getUser_InactiveUser_ThrowsException() {
        User inactiveUser = User.builder().id(1L).active(false).build();
        when(userRepository.findById(1L)).thenReturn(inactiveUser);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> userService.getUser(1L));

        assertEquals(ErrorCode.OWNER_DELETED, ex.getErrorCode());
    }

    @Test
    void getCurrentUser_AuthenticatedActiveUser_ReturnsUser() {
        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            User expectedUser = User.builder()
                    .id(1L)
                    .nickname("testuser")
                    .email("test@example.com")
                    .active(true)
                    .build();

            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(expectedUser);

            UserResponse response = userService.getCurrentUser();

            assertNotNull(response);
            assertEquals(expectedUser.id(), response.getUserId());
            assertEquals(expectedUser.nickname(), response.getNickname());
        }
    }

    @Test
    void getCurrentUser_Unauthenticated_ThrowsException() {
        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(null);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> userService.getCurrentUser());

            assertEquals(ErrorCode.USER_NOT_AUTHORIZED, ex.getErrorCode());
        }
    }

    @Test
    void getCurrentUser_UserInactive_ThrowsException() {
        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            User inactiveUser = User.builder().id(1L).active(false).build();
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(inactiveUser);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> userService.getCurrentUser());

            assertEquals(ErrorCode.CURRENT_USER_DELETED, ex.getErrorCode());
        }
    }

    @Test
    void updateUser_AdminUpdatesOtherUser_Success() {
        User admin = User.builder()
                .id(1L)
                .admin(true)
                .active(true)
                .build();
        User targetUser = User.builder()
                .id(2L)
                .active(true)
                .build();

        when(userRepository.findById(2L)).thenReturn(targetUser);
        when(passwordEncoder.encode("newpass")).thenReturn("encoded");

        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(admin);

            userService.updateUser(2L, "nick", "desc", "newpass", null);
        }

        verify(userRepository).updateUser(eq(2L), eq("nick"), eq("desc"), eq("encoded"), any());
    }

    @Test
    void updateCurrentUser_RemoveAvatar_UpdatesWithoutFile() {
        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            User currentUser = User.builder()
                    .id(1L)
                    .avatar(UUID.randomUUID())
                    .active(true)
                    .build();

            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(currentUser);

            userService.updateCurrentUser(null, null, null, null);

            verify(userRepository).updateUser(eq(1L), isNull(), isNull(), isNull(), isNull());
            verify(minioService, never()).deleteAvatar(any());
        }
    }

    @Test
    void deleteUser_UserDeletesSelf_Success() {
        User user = User.builder()
                .id(1L)
                .active(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(user);

        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(user);

            userService.deleteUser(1L);
        }

        verify(userRepository).deleteUser(1L);
        verify(refreshTokenRepository).deleteByUser(1L);
    }

    @Test
    void updateCurrentUser_InvalidAvatarFormat_ThrowsException() {
        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            User currentUser = User.builder().id(1L).active(true).build();
            MockMultipartFile invalidFile = new MockMultipartFile(
                    "avatar", "test.png", "image/png", "content".getBytes());

            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(currentUser);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> userService.updateCurrentUser(null, null, null, invalidFile));

            assertEquals(ErrorCode.FILE_FORMAT_NOT_SUPPORTED, ex.getErrorCode());
        }
    }

    @Test
    void deleteAvatar_RemovesOldAvatarFromMinio() {
        UUID oldAvatar = UUID.randomUUID();
        User user = User.builder()
                .id(1L)
                .avatar(oldAvatar)
                .active(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(user);
        doNothing().when(minioService).deleteAvatar(any());

        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(user);

            userService.deleteAvatar(1L);
        }

        verify(minioService).deleteAvatar(oldAvatar);
        verify(userRepository).deleteAvatar(1L);
    }

    @Test
    void deleteCurrentAvatar_WithExistingAvatar_RemovesFromStorage() {
        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            UUID oldAvatar = UUID.randomUUID();
            User currentUser = User.builder()
                    .id(1L)
                    .avatar(oldAvatar)
                    .active(true)
                    .build();

            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(currentUser);

            userService.deleteCurrentAvatar();

            verify(userRepository).deleteAvatar(1L);
            verify(minioService).deleteAvatar(oldAvatar);
        }
    }

    @Test
    void deleteCurrentAvatar_NoAvatar_DoesNothing() {
        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            User currentUser = User.builder()
                    .id(1L)
                    .avatar(null)
                    .active(true)
                    .build();

            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(currentUser);

            userService.deleteCurrentAvatar();

            verify(userRepository, never()).deleteAvatar(anyLong());
            verify(minioService, never()).deleteAvatar(any());
        }
    }

    @Test
    void updateUser_WhenUpdatingOtherUserAsAdmin_ShouldUpdateSuccessfully() {
        User admin = User.builder()
                .id(1L)
                .admin(true)
                .active(true)
                .build();
        User targetUser = User.builder()
                .id(2L)
                .avatar(UUID.randomUUID())
                .active(true)
                .build();

        when(userRepository.findById(2L)).thenReturn(targetUser);
        when(passwordEncoder.encode("newpass")).thenReturn("encoded");

        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(admin);

            userService.updateUser(2L, "newnick", "newdesc", "newpass", null);
        }

        verify(userRepository).updateUser(eq(2L), eq("newnick"), eq("newdesc"), eq("encoded"), isNull());
    }

    @Test
    void deleteUser_WhenUserIsInactive_ShouldThrowException() {
        User user = User.builder().id(1L).active(false).build();
        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(user);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> userService.deleteUser(1L));

            assertEquals(ErrorCode.CURRENT_USER_DELETED, ex.getErrorCode());
        }
    }

    @Test
    void deleteAvatar_WhenNoAvatarExists_ShouldDoNothing() {
        User user = User.builder()
                .id(1L)
                .avatar(null)
                .active(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(user);
        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(user);

            assertDoesNotThrow(() -> userService.deleteAvatar(1L));
        }

        verify(minioService, never()).deleteAvatar(any());
    }
}