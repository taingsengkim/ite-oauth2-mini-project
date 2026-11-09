package kh.edu.istad.platform.iam.rest.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetUserPasswordRequest(
        @NotBlank @Size(max = 72) String newPassword,
        @NotBlank @Size(max = 72) String confirmPassword
) {

    @Override
    public String toString() {
        return "ResetUserPasswordRequest[REDACTED]";
    }
}