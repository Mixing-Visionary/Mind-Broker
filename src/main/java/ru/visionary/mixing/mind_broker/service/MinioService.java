package ru.visionary.mixing.mind_broker.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.visionary.mixing.mind_broker.config.properties.MinioProperties;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {
    private final MinioClient minioClient;
    private final MinioProperties properties;

    public void uploadFile(MultipartFile file, UUID uuid) {
        log.info("Uploading file {} to MinIO}", uuid);
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(uuid.toString())
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            log.debug("Successfully uploaded file {}", uuid);
        } catch (Exception e) {
            log.error("Failed to upload file {} to MinIO: {}", uuid, e.getMessage());
            throw new ServiceException(ErrorCode.FAILED_UPLOAD_MINIO);
        }
    }

    public void deleteFile(UUID uuid) {
        log.debug("Starting MinIO deletion. UUID: {}", uuid);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(uuid.toString())
                    .build());
            log.info("MinIO deletion success. UUID: {}", uuid);
        } catch (Exception e) {
            log.error("MinIO deletion failed. UUID: {}, Error: {}", uuid, e.getMessage());
            throw new ServiceException(ErrorCode.FAILED_DELETE_MINIO);
        }
    }
}
