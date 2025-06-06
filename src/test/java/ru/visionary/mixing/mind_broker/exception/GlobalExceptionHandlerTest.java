package ru.visionary.mixing.mind_broker.exception;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import ru.visionary.mixing.generated.model.ErrorResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {
    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void handleServiceException_ReturnsCorrectStatus() {
        ServiceException ex = new ServiceException(ErrorCode.INVALID_REFRESH_TOKEN);
        ResponseEntity<Object> response = handler.handleServiceException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(-5, ((ErrorResponse) response.getBody()).getErrorCode());
    }

    @Test
    void handleConstraintViolation_ReturnsBadRequest() {
        ConstraintViolationException ex = mock(ConstraintViolationException.class);
        ResponseEntity<Object> response = handler.handleConstraintViolationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleMethodArgumentNotValid_ReturnsBadRequest() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(ex, null, HttpStatus.BAD_REQUEST, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleDatabaseException_Returns500() {
        DataAccessException ex = mock(DataAccessException.class);
        ResponseEntity<Object> response = handler.handleDataAccessException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(-100, ((ErrorResponse) response.getBody()).getErrorCode());
    }

    @Test
    void handleFileUploadException_Returns413() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(1000);
        ResponseEntity<Object> response = handler.handleMaxUploadSizeExceededException(ex, null, null, null);

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
    }
}