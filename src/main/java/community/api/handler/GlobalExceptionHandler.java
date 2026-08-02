package community.api.handler;

import community.api.exception.BusinessException;
import community.api.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();

        for (FieldError fieldError : fieldErrors) {
            String field = fieldError.getField();
            String message = fieldError.getDefaultMessage();

            errors.put(field, message);
        }
        return ResponseEntity.badRequest()
                .body(ApiResponse.of("invalid_request", errors));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(
            BusinessException exception
    ) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(ApiResponse.of(exception.getCode(), null));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception exception
    ) {
        log.error("Unhandled server exception", exception);

        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.of("internal_server_error", null));
    }
}
