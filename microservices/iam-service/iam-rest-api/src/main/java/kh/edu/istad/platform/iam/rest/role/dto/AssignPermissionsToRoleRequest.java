package kh.edu.istad.platform.iam.rest.role.dto;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

public record AssignPermissionsToRoleRequest(
    @NotEmpty @Size(max = 100) List<@NotNull UUID> permissionUuids,
    Instant expiresAt,
    @Size(max = 255) String reason
) {}
