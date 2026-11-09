package kh.edu.istad.platform.iam.rest.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(max = 72) String newPassword,
        @NotBlank @Size(max = 72) String confirmPassword
) {

    @Override
    public String toString() {
        return "ChangePasswordRequest[REDACTED]";
    }
}
