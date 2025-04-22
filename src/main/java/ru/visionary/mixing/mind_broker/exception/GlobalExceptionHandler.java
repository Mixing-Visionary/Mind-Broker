package ru.visionary.mixing.mind_broker.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.visionary.mixing.generated.model.ErrorResponse;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Object> handleDataAccessException(DataAccessException ex) {
        log.error("Database error occurred: {}", ex.getMessage());
        return createResponse(ErrorCode.DATABASE_EXCEPTION);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("Database error occurred: {}", ex.getMessage());
        return createResponse(ErrorCode.DATABASE_EXCEPTION);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return createResponse(ErrorCode.INVALID_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return createResponse(ErrorCode.INVALID_REQUEST);
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Object> handleServiceException(ServiceException se) {
        log.warn("Service exception: {} - {}", se.getErrorCode().name(), se.getErrorCode().getMessage());
        return createResponse(se.getErrorCode());
    }

    private ResponseEntity<Object> createResponse(ErrorCode errorCode) {
        return new ResponseEntity<>(new ErrorResponse(errorCode.getErrorCode(), errorCode.getMessage()), errorCode.getStatus());
    }
}
