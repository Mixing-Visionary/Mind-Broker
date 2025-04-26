package ru.visionary.mixing.mind_broker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.repository.ImageRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}