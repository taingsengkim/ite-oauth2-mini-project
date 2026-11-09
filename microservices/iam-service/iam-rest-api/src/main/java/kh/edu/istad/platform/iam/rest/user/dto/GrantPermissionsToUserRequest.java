package kh.edu.istad.platform.iam.rest.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kh.edu.istad.platform.iam.model.PermissionEffect;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GrantPermissionsToUserRequest(
        @NotEmpty @Size(max = 100)
        List<@NotNull UUID> permissionUuids,

        @NotNull
        PermissionEffect effect,

        Instant expiresAt,

        @Size(max = 255)
        String reason
) {}
