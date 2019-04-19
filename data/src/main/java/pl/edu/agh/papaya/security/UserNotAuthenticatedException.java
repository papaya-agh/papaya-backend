package pl.edu.agh.papaya.security;

public class UserNotAuthenticatedException extends RuntimeException {

    public UserNotAuthenticatedException() {

    }

    public UserNotAuthenticatedException(String message) {
        super(message);
    }
}
