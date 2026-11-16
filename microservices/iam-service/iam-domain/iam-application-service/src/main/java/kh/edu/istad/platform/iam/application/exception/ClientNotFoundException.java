package kh.edu.istad.platform.iam.application.exception;

public final class ClientNotFoundException extends RuntimeException {
    public ClientNotFoundException() { super("The OAuth client was not found"); }
}
