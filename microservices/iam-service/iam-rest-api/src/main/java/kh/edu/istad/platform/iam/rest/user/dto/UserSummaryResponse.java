package kh.edu.istad.platform.iam.rest.user.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserSummaryResponse(UUID uuid, String username, String email, boolean emailVerified,
                                  String name, String locale, String pictureUrl, boolean enabled, boolean accountNonLocked, List<RoleReferenceResponse> roles,
                                  Instant createdAt, Instant updatedAt) {
    public UserSummaryResponse { roles = List.copyOf(roles); }
}
