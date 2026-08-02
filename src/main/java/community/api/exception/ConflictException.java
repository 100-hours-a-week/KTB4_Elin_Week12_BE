package community.api.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BusinessException {

    public ConflictException(String code) {
        super(code, HttpStatus.CONFLICT);
    }
}