package ru.visionary.mixing.mind_broker.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST(-1, "Некорректный запрос", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(-2, "Неверные учетные данные", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_IN_USE(-3, "Почта уже используется", HttpStatus.CONFLICT),
    NICKNAME_ALREADY_IN_USE(-4, "Никнейм уже используется", HttpStatus.CONFLICT),
    INVALID_REFRESH_TOKEN(-5, "Refresh токен не валиден", HttpStatus.NOT_FOUND),
    EXPIRED_REFRESH_TOKEN(-6, "Refresh токен истек", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(-7, "Пользователь не найден", HttpStatus.NOT_FOUND),
    DATABASE_EXCEPTION(-100, "Ошибка записи в БД", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int errorCode;
    private final String message;
    private final HttpStatus status;
}
