package kh.edu.istad.platform.iam.validation;

import kh.edu.istad.platform.iam.exception.IamDomainException;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import kh.edu.istad.platform.iam.port.out.configuration.SystemConfigurationRepository;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class PasswordPolicy {
    private final SystemConfigurationRepository configuration;

    public void validate(String password) {
        if (password == null || password.isBlank()) {
            invalid("Password is required");
        }

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            invalid("Password must not exceed 72 UTF-8 bytes");
        }
        var policy = configuration.current().policy();
        int length = password.codePointCount(0, password.length());
        if (length < policy.passwordMinLength() || length > policy.passwordMaxLength())
            invalid("Password must contain " + policy.passwordMinLength() + "–" + policy.passwordMaxLength() + " characters");
        if (policy.passwordRequireUppercase() && password.codePoints().noneMatch(Character::isUpperCase)) invalid("Password must include an uppercase letter");
        if (policy.passwordRequireLowercase() && password.codePoints().noneMatch(Character::isLowerCase)) invalid("Password must include a lowercase letter");
        if (policy.passwordRequireDigit() && password.codePoints().noneMatch(Character::isDigit)) invalid("Password must include a digit");
        if (policy.passwordRequireSpecial() && password.codePoints().noneMatch(PasswordPolicy::isSpecial)) invalid("Password must include a special character");
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
    private static boolean isSpecial(int value) {
        return switch (Character.getType(value)) {
            case Character.CONNECTOR_PUNCTUATION, Character.DASH_PUNCTUATION, Character.START_PUNCTUATION,
                 Character.END_PUNCTUATION, Character.INITIAL_QUOTE_PUNCTUATION, Character.FINAL_QUOTE_PUNCTUATION,
                 Character.OTHER_PUNCTUATION, Character.MATH_SYMBOL, Character.CURRENCY_SYMBOL,
                 Character.MODIFIER_SYMBOL, Character.OTHER_SYMBOL -> true;
            default -> false;
        };
    }
}
