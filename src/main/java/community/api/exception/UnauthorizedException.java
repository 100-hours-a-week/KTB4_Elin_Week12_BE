package community.api.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String code) {
        super(code, HttpStatus.UNAUTHORIZED);
    }
}