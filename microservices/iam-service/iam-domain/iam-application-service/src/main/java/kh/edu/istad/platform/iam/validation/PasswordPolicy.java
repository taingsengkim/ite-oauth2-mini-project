package kh.edu.istad.platform.iam.validation;

import kh.edu.istad.platform.iam.exception.IamDomainException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class PasswordPolicy {

    public void validate(String password) {
        if (password == null || password.isBlank()) {
            invalid("Password is required");
        }

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            invalid("Password must not exceed 72 UTF-8 bytes");
        }
    }

    public void validate(String password, String confirmation) {
        validate(password);

        if (!password.equals(confirmation)) {
            invalid("Passwords do not match");
        }
    }

    private static void invalid(String message) {
        throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, message);
    }
}