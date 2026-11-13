package kh.edu.istad.platform.iam.dto.user;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserSummary(UUID uuid, String username, String email, boolean emailVerified,
                          String name, String locale, String pictureUrl, boolean enabled, boolean accountNonLocked, List<RoleReference> roles,
                          Instant createdAt, Instant updatedAt) {
    public UserSummary { roles = List.copyOf(roles); }
}
