package kh.edu.istad.platform.iam.dto.user;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

public record UserResult(
        UUID uuid,
        String username,
        String email,
        boolean emailVerified,
        String givenName,
        String familyName,
        String name,
        String locale,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt,
        List<RoleReference> roles
) {
    public UserResult { roles = List.copyOf(roles); }
}