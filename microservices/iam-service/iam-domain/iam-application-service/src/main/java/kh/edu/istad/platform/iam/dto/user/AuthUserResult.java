package kh.edu.istad.platform.iam.dto.user;

import java.util.List;
import java.util.UUID;

public record AuthUserResult(
        UUID uuid,
        String username,
        String passwordHash,
        boolean enabled,
        boolean accountNonExpired,
        boolean accountNonLocked,
        boolean credentialsNonExpired,
        List<String> authorities
) {

    public AuthUserResult {
        authorities = List.copyOf(authorities);
    }

    @Override
    public String toString() {
        return "AuthUserResult[uuid=" + uuid + "]";
    }
}