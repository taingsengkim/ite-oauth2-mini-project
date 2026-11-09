package kh.edu.istad.platform.iam.dto.permission;

public record CreatePermissionCommand(
        String resource,
        String action,
        String description
) {
}
