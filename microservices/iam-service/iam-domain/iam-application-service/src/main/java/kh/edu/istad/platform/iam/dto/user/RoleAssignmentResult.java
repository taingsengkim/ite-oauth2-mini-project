package kh.edu.istad.platform.iam.dto.user;

import java.time.Instant;
import java.util.UUID;

public record RoleAssignmentResult(
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