package kh.edu.istad.platform.iam.web.form;

import jakarta.validation.constraints.*;

public record PasswordForm(
        @NotBlank String currentPassword,
        @NotBlank @Size(max = 72) String newPassword,
        @NotBlank String confirmPassword
) {
    @Override public String toString() { return "PasswordForm[redacted]"; }
    public static PasswordForm empty() { return new PasswordForm("", "", ""); }
}
