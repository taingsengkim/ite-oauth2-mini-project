package kh.edu.istad.platform.iam.rest.user.dto;

import java.time.Instant;
import java.util.UUID;

public record RoleAssignmentResponse(
        Long id,
        UUID userUuid,
        UUID roleUuid,
        Instant assignedAt,
        String assignedBy,
        Instant expiresAt,
        Instant revokedAt,
        String revokedBy,
        String reason
) {}
