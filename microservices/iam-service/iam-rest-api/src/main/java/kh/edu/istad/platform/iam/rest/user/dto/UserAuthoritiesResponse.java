
package kh.edu.istad.platform.iam.rest.user.dto;

import kh.edu.istad.platform.iam.model.PermissionEffect;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserAuthoritiesResponse(
        List<RoleAuthority> roles,
        List<DirectPermission> directPermissions,
        List<String> effectivePermissions
) {

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