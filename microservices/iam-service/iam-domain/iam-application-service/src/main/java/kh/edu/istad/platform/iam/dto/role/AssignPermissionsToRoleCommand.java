package kh.edu.istad.platform.iam.dto.role;

import java.util.UUID;
import java.util.List;
import java.time.Instant;

public record AssignPermissionsToRoleCommand(UUID roleUuid, List<UUID> permissionUuids, Instant expiresAt, String reason) {}
