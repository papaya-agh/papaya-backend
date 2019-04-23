package pl.edu.agh.papaya.security;

public class UserNotAuthenticatedException extends RuntimeException {

    public UserNotAuthenticatedException() {
        super();
    }

    public UserNotAuthenticatedException(String message) {
        super(message);
    }
}
