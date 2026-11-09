package kh.edu.istad.platform.iam.application.exception;

public final class AuthenticationRequiredException extends RuntimeException {

    public AuthenticationRequiredException() {
        super("Authentication is required");
    }
}
