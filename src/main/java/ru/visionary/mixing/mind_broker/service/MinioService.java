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

    public void uploadImage(MultipartFile image, UUID uuid) {
        uploadFile(image, uuid, properties.getImagesBucket());
    }

    public void uploadAvatar(MultipartFile avatar, UUID uuid) {
        uploadFile(avatar, uuid, properties.getAvatarsBucket());
    }

    private void uploadFile(MultipartFile file, UUID uuid, String bucket) {
        log.info("Uploading file {} to MinIO bucket {}}", uuid, bucket);
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(uuid.toString())
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            log.debug("Successfully uploaded file {} to bucket {}", uuid, bucket);
        } catch (Exception e) {
            log.error("Failed to upload file {} to MinIO bucket {}: {}", uuid, bucket, e.getMessage());
            throw new ServiceException(ErrorCode.FAILED_UPLOAD_MINIO);
        }
    }

    public void deleteImage(UUID uuid) {
        deleteFile(uuid, properties.getImagesBucket());
    }

    public void deleteAvatar(UUID uuid) {
        deleteFile(uuid, properties.getAvatarsBucket());
    }

    private void deleteFile(UUID uuid, String bucket) {
        log.debug("Deleting file {} from MinIO bucket {}", uuid, bucket);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(uuid.toString())
                    .build());
            log.info("Successfully deleted file {} from bucket {}", uuid, bucket);
        } catch (Exception e) {
            log.error("Failed to delete file {} from MinIO bucket {}: {}", uuid, bucket, e.getMessage());
            throw new ServiceException(ErrorCode.FAILED_DELETE_MINIO);
        }
    }
}
