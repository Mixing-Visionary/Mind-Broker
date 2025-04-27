package ru.visionary.mixing.mind_broker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.repository.RefreshTokenRepository;
import ru.visionary.mixing.mind_broker.repository.UserRepository;
import ru.visionary.mixing.mind_broker.utils.SecurityContextUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @InjectMocks
    private UserService userService;

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

            assertEquals(ErrorCode.USER_DELETED, ex.getErrorCode());
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