package kh.edu.istad.platform.iam.rest.role.dto;
import java.time.Instant;
import java.util.UUID;

public record PermissionAssignmentResponse(Long id, UUID roleUuid, UUID permissionUuid,
        Instant assignedAt, String assignedBy, Instant expiresAt,
        Instant revokedAt, String revokedBy, String reason) {}
