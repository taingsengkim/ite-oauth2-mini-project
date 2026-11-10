package kh.edu.istad.platform.iam.application.exception;

public final class ProfileCompletionRequiredException extends RuntimeException {
    public ProfileCompletionRequiredException() {
        super("Required local profile information is missing");
    }
}
