package kh.edu.istad.platform.iam.dto.user;

import kh.edu.istad.platform.iam.model.PermissionEffect;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserAuthoritiesResult(
        List<RoleAuthority> roles,
        List<DirectPermission> directPermissions,
        List<String> effectivePermissions
) {

    public UserAuthoritiesResult {
        roles = List.copyOf(roles);
        directPermissions = List.copyOf(directPermissions);
        effectivePermissions = List.copyOf(effectivePermissions);
    }

    public record RoleAuthority(
            UUID uuid,
            String name,
            Instant expiresAt
    ) {}

    public record DirectPermission(
            String name,
            PermissionEffect effect,
            Instant expiresAt
    ) {}
}
