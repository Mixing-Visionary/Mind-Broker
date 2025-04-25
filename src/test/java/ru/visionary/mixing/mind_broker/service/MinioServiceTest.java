package ru.visionary.mixing.mind_broker.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.visionary.mixing.mind_broker.config.properties.MinioProperties;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {
    @Mock
    private MinioClient minioClient;
    @Mock
    private MinioProperties minioProperties;

    @InjectMocks
    private MinioService minioService;

    @Test
    void uploadFile_SuccessfulUpload_NoExceptions() throws Exception {
        MultipartFile file = new MockMultipartFile(
                "test.jpg",
                "test.jpg",
                "image/jpeg",
                new byte[10]
        );

        doReturn("test-bucket").when(minioProperties).getBucketName();

        assertDoesNotThrow(() -> minioService.uploadFile(file, UUID.randomUUID()));
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void uploadFile_UploadFailure_ThrowsException() throws Exception {
        MultipartFile file = new MockMultipartFile(
                "test.jpg",
                "test.jpg",
                "image/jpeg",
                new byte[10]
        );

        doReturn("test-bucket").when(minioProperties).getBucketName();
        doThrow(new RuntimeException()).when(minioClient).putObject(any(PutObjectArgs.class));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> minioService.uploadFile(file, UUID.randomUUID()));

        assertEquals(ErrorCode.FAILED_UPLOAD_MINIO, exception.getErrorCode());
    }
}