package pl.edu.agh.papaya.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ForbiddenAccessException extends RuntimeException {

    public ForbiddenAccessException() {
        super();
    }

    public ForbiddenAccessException(Throwable cause) {
        super(cause);
    }
}
