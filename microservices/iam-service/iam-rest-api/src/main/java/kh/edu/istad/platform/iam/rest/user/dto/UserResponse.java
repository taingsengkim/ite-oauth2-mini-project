package kh.edu.istad.platform.iam.rest.user.dto;
import java.time.Instant;
import java.util.UUID;
import java.util.List;

public record UserResponse(
        UUID uuid,
        String username,
        String email,
        boolean emailVerified,
        String givenName,
        String familyName,
        String name,
        String locale,
        String pictureUrl,
        String nickname,
        String phoneNumber,
        boolean enabled,
        boolean accountNonLocked,
        Instant createdAt,
        Instant updatedAt,
        List<RoleReferenceResponse> roles
) {
    public UserResponse { roles = List.copyOf(roles); }
}
