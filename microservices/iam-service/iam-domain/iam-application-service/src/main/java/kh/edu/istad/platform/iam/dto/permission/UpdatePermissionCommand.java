package kh.edu.istad.platform.iam.dto.permission;

import java.util.UUID;

public record UpdatePermissionCommand (
        UUID uuid,
        String resource,
        String action,
        String description
) {
}
