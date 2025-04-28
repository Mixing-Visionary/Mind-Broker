package ru.visionary.mixing.mind_broker.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ImageUtilsTest {
    @Test
    void checkImage_InvalidPng_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "test.png",
                "test.png",
                "image/png",
                new byte[100]
        );

        ServiceException ex = assertThrows(ServiceException.class,
                () -> ImageUtils.checkImage(file));

        assertEquals(ErrorCode.FILE_FORMAT_NOT_SUPPORTED, ex.getErrorCode());
    }

    @Test
    void checkImage_EmptyFile_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "empty.jpg",
                new byte[0]
        );

        ServiceException ex = assertThrows(ServiceException.class,
                () -> ImageUtils.checkImage(file));

        assertEquals(ErrorCode.EMPTY_FILE, ex.getErrorCode());
    }
}