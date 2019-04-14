package pl.edu.agh.papaya.security;

public class AuthenticationException extends Exception {

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
