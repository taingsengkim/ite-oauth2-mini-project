package kh.edu.istad.platform.iam.dto.user;

import kh.edu.istad.platform.iam.model.PermissionEffect;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GrantPermissionsToUserCommand(
        UUID userUuid,
        List<UUID> permissionUuids,
        PermissionEffect effect,
        Instant expiresAt,
        String reason
) {}