package kh.edu.istad.platform.iam.dto.role;

import java.util.UUID;
import java.time.Instant;

public record PermissionAssignmentResult(Long id, UUID roleUuid, UUID permissionUuid, Instant assignedAt, String assignedBy, Instant expiresAt, Instant revokedAt, String revokedBy, String reason) {}
