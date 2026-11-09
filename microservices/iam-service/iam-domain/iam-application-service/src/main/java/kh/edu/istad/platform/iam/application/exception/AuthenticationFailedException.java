package kh.edu.istad.platform.iam.application.exception;

public final class AuthenticationFailedException extends RuntimeException {

    public AuthenticationFailedException() {
        super("Invalid username or password");
    }
}
