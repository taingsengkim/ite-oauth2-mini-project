package kh.edu.istad.platform.iam.dto.user;

import java.util.UUID;

public record ChangePasswordCommand(
        UUID userUuid,
        String currentPassword,
        String newPassword,
        String confirmPassword
) {

    @Override
    public String toString() {
        return "ChangePasswordCommand[userUuid=" + userUuid + "]";
    }
}