package pl.edu.agh.papaya.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {

    public ConflictException() {
        super();
    }

    public ConflictException(String message) {
        super(message);
    }
}
