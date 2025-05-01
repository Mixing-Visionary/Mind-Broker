package ru.visionary.mixing.mind_broker.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST(-1, "Bad request", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(-2, "Invalid credentials", HttpStatus.UNAUTHORIZED),
    EMAIL_ALREADY_IN_USE(-3, "Email already in use", HttpStatus.CONFLICT),
    NICKNAME_ALREADY_IN_USE(-4, "Nickname already in use", HttpStatus.CONFLICT),
    INVALID_REFRESH_TOKEN(-5, "Invalid refresh token", HttpStatus.NOT_FOUND),
    EXPIRED_REFRESH_TOKEN(-6, "Expired refresh token", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(-7, "User is not found", HttpStatus.NOT_FOUND),
    FILE_FORMAT_NOT_SUPPORTED(-8, "File format is not supported", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    EMPTY_FILE(-9, "Empty file", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(-10, "File too large", HttpStatus.PAYLOAD_TOO_LARGE),
    USER_NOT_AUTHORIZED(-11, "User is not authorized", HttpStatus.UNAUTHORIZED),
    ACCESS_FORBIDEN(-12, "Access forbidden", HttpStatus.FORBIDDEN),
    IMAGE_NOT_FOUND(-13, "Image is not found", HttpStatus.NOT_FOUND),
    USER_DELETED(-14, "User is deleted", HttpStatus.GONE),
    STYLE_NOT_FOUND(-15, "Style is not found", HttpStatus.NOT_FOUND),
    STYLE_NOT_SUPPORTED(-16, "Style is not supported", HttpStatus.GONE),
    DATABASE_EXCEPTION(-100, "Database exception", HttpStatus.INTERNAL_SERVER_ERROR),
    FAILED_UPLOAD_MINIO(-101, "Failed upload file to MinIO", HttpStatus.INTERNAL_SERVER_ERROR),
    FAILED_DELETE_MINIO(-102, "Failed delete file from MinIO", HttpStatus.INTERNAL_SERVER_ERROR),
    FAILED_PUSH_TO_RABBIT(-103, "Failed push message to RabbitMQ", HttpStatus.INTERNAL_SERVER_ERROR),
    MEGAMIND_ERROR(-104, "Error on megamind", HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_ERROR(-105, "Internal error", HttpStatus.INTERNAL_SERVER_ERROR),
    RABBIT_ERROR(-105, "Rabbit exception", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int errorCode;
    private final String message;
    private final HttpStatus status;

    @Override
    public String toString() {
        return "{" +
                "\"errorCode\":\"" + errorCode + "\", " +
                "\"message\":\"" + message + "\"" +
                '}';
    }
}
