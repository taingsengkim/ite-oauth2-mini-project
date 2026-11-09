package kh.edu.istad.platform.iam.exception;

import kh.edu.istad.common.domain.exception.DomainException;

import java.util.Objects;

public final class IamDomainException extends DomainException {

    public enum Code {
        VALIDATION_ERROR,
        RULE_VIOLATION
    }

    private final Code code;

    public IamDomainException(Code code, String message) {
        super(Objects.requireNonNull(message, "message"));
        this.code = Objects.requireNonNull(code, "code");
    }

    public Code getCode() {
        return code;
    }
}
