package kh.edu.istad.platform.iam.application.exception;

public final class ResourceConflictException extends RuntimeException {
    public ResourceConflictException(String message) {
        super(message);
    }
}
