
package kh.edu.istad.platform.iam.dto.user;

import java.util.UUID;

public record ResetUserPasswordCommand(
        UUID userUuid,
        String newPassword,
        String confirmPassword
) {

    @Override
    public String toString() {
        return "ResetUserPasswordCommand[userUuid=" + userUuid + "]";
    }
}