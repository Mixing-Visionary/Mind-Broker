package ru.visionary.mixing.mind_broker.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST(-1, "Некорректный запрос", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(-2, "Неверные учетные данные", HttpStatus.UNAUTHORIZED),
    EMAIL_ALREADY_IN_USE(-3, "Почта уже используется", HttpStatus.CONFLICT),
    NICKNAME_ALREADY_IN_USE(-4, "Никнейм уже используется", HttpStatus.CONFLICT),
    INVALID_REFRESH_TOKEN(-5, "Refresh токен не валиден", HttpStatus.NOT_FOUND),
    EXPIRED_REFRESH_TOKEN(-6, "Refresh токен истек", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(-7, "Пользователь не найден", HttpStatus.NOT_FOUND),
    FILE_FORMAT_NOT_SUPPORTED(-8, "Файл в неподдерживаемом формате", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    EMPTY_FILE(-9, "Пустой файл", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(-10, "Файл слишком большой", HttpStatus.PAYLOAD_TOO_LARGE),
    USER_NOT_AUTHORIZED(-11, "Пользователь не авторизован", HttpStatus.UNAUTHORIZED),
    ACCESS_FORBIDEN(-12, "Нет доступа", HttpStatus.FORBIDDEN),
    DATABASE_EXCEPTION(-100, "Ошибка записи в БД", HttpStatus.INTERNAL_SERVER_ERROR),
    FAILED_UPLOAD_MINIO(-101, "Не удалось загрузить файл в MinIO", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int errorCode;
    private final String message;
    private final HttpStatus status;
}
