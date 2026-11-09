package kh.edu.istad.platform.iam.dto.user;

import kh.edu.istad.platform.iam.model.PermissionEffect;

import java.time.Instant;
import java.util.UUID;

public record PermissionGrantResult(
        Long id,
        UUID userUuid,
        UUID permissionUuid,
        PermissionEffect effect,
        Instant assignedAt,
        String assignedBy,
        Instant expiresAt,
        Instant revokedAt,
        String revokedBy,
        String reason
) {}