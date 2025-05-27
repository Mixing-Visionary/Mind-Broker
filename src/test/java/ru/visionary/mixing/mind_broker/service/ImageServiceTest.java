package ru.visionary.mixing.mind_broker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.visionary.mixing.generated.model.UpdateImageRequest;
import ru.visionary.mixing.mind_broker.entity.Image;
import ru.visionary.mixing.mind_broker.entity.Protection;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.repository.ImageRepository;
import ru.visionary.mixing.mind_broker.utils.SecurityContextUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private MinioService minioService;

    @InjectMocks
    private ImageService imageService;

    @Test
    void saveImage_EmptyFile_ThrowsException() {
        MultipartFile file = new MockMultipartFile(
                "empty.jpg",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        ServiceException exception = assertThrows(ServiceException.class,
                () -> imageService.saveImage(file, "public"));

        assertEquals(ErrorCode.EMPTY_FILE, exception.getErrorCode());
    }

    @Test
    void saveImage_InvalidFileFormat_ThrowsException() {
        MultipartFile file = new MockMultipartFile(
                "test.png",
                "test.png",
                "image/png",
                new byte[10]
        );

        ServiceException exception = assertThrows(ServiceException.class,
                () -> imageService.saveImage(file, "public"));

        assertEquals(ErrorCode.FILE_FORMAT_NOT_SUPPORTED, exception.getErrorCode());
    }

    @Test
    void saveImage_InvalidProtectionValue_ThrowsValidationException() {
        MultipartFile file = validImageFile();

        ServiceException exception = assertThrows(ServiceException.class,
                () -> imageService.saveImage(file, "invalid-protection"));

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
    }

    @Test
    void getImage_PrivateImageWithoutAccess_ThrowsForbidden() {
        UUID uuid = UUID.randomUUID();
        User owner = User.builder()
                .id(1L)
                .active(true)
                .build();
        User requester = User.builder()
                .id(2L)
                .active(true)
                .build();
        Image image = Image.builder()
                .id(uuid)
                .owner(owner)
                .protection(Protection.PRIVATE)
                .build();

        when(imageRepository.findById(uuid)).thenReturn(image);
        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(requester);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> imageService.getImage(uuid));

            assertEquals(ErrorCode.ACCESS_FORBIDDEN, ex.getErrorCode());
        }
    }

    @Test
    void getImage_NonExistingImage_ThrowsNotFoundException() {
        UUID nonExistingId = UUID.randomUUID();
        when(imageRepository.findById(nonExistingId)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> imageService.getImage(nonExistingId));

        assertEquals(ErrorCode.IMAGE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getImagesForCurrentUser_InvalidProtection_ThrowsException() {
        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            utils.when(SecurityContextUtils::getAuthenticatedUser)
                    .thenReturn(User.builder().active(true).build());

            assertThrows(ServiceException.class,
                    () -> imageService.getImagesForCurrentUser(10, 0, "invalid"));
        }
    }

    @Test
    void updateImage_ChangeToPrivate_UpdatesProtection() {
        UUID uuid = UUID.randomUUID();
        UpdateImageRequest request = new UpdateImageRequest().protection(UpdateImageRequest.ProtectionEnum.PRIVATE);
        User owner = User.builder()
                .id(1L)
                .active(true)
                .build();
        Image image = Image.builder().id(uuid).owner(owner).build();

        when(imageRepository.findById(uuid)).thenReturn(image);
        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(owner);

            assertDoesNotThrow(() -> imageService.updateImage(uuid, request));
            verify(imageRepository).updateProtection(uuid, Protection.PRIVATE);
        }
    }

    @Test
    void updateImage_AdminUpdatesProtection_SuccessfullyUpdates() {
        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            User admin = User.builder()
                    .id(1L)
                    .nickname("testuser")
                    .email("test@example.com")
                    .password("encoded-pass")
                    .active(true)
                    .admin(true)
                    .build();
            UUID imageId = UUID.randomUUID();
            Image image = Image.builder()
                    .id(imageId)
                    .owner(createTestUser(true))
                    .protection(Protection.PUBLIC)
                    .build();

            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(admin);
            when(imageRepository.findById(imageId)).thenReturn(image);

            UpdateImageRequest request = new UpdateImageRequest()
                    .protection(UpdateImageRequest.ProtectionEnum.PRIVATE);

            imageService.updateImage(imageId, request);

            verify(imageRepository).updateProtection(imageId, Protection.PRIVATE);
        }
    }

    @Test
    void deleteById_AdminUser_DeletesSuccessfully() {
        UUID uuid = UUID.randomUUID();
        User admin = User.builder()
                .id(1L)
                .admin(true)
                .active(true)
                .build();
        Image image = Image.builder()
                .id(uuid)
                .owner(User.builder().id(2L).active(true).build())
                .build();

        when(imageRepository.findById(uuid)).thenReturn(image);
        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(admin);

            assertDoesNotThrow(() -> imageService.deleteById(uuid));
            verify(imageRepository).deleteById(uuid);
            verify(minioService).deleteImage(uuid);
        }
    }

    @Test
    void deleteById_WhenImageOwnerInactive_ThrowsException() {
        UUID uuid = UUID.randomUUID();
        Image image = Image.builder()
                .owner(User.builder().active(false).build())
                .build();

        when(imageRepository.findById(uuid)).thenReturn(image);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> imageService.deleteById(uuid));

        assertEquals(ErrorCode.OWNER_DELETED, ex.getErrorCode());
    }

    @Test
    void deleteImage_OwnerDeletesImage_RemovesFromStorage() {
        try (MockedStatic<SecurityContextUtils> utils = mockStatic(SecurityContextUtils.class)) {
            User owner = createTestUser(true);
            UUID imageId = UUID.randomUUID();
            Image image = Image.builder()
                    .id(imageId)
                    .owner(owner)
                    .build();

            utils.when(SecurityContextUtils::getAuthenticatedUser).thenReturn(owner);
            when(imageRepository.findById(imageId)).thenReturn(image);

            imageService.deleteById(imageId);

            verify(imageRepository).deleteById(imageId);
            verify(minioService).deleteImage(imageId);
        }
    }

    private User createTestUser(boolean active) {
        return User.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .password("encoded-pass")
                .active(active)
                .build();
    }

    private Image createTestImage(User owner, Protection protection) {
        return Image.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .protection(protection)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private MultipartFile validImageFile() {
        return new MockMultipartFile(
                "image.jpg",
                "image.jpg",
                "image/jpeg",
                new byte[100]
        );
    }
}