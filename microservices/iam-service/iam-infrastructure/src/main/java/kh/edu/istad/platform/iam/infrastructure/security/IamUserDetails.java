package kh.edu.istad.platform.iam.infrastructure.security;

import kh.edu.istad.platform.iam.dto.user.AuthUserResult;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.io.Serial;
import java.util.UUID;

public final class IamUserDetails extends User {
    @Serial
    private static final long serialVersionUID = 1L;
    private final UUID uuid;

    public IamUserDetails(AuthUserResult result) {
        super(
                result.username(),
                result.passwordHash(),
                result.enabled(),
                result.accountNonExpired(),
                result.credentialsNonExpired(),
                result.accountNonLocked(),
                result.authorities().stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList()
        );

        this.uuid = result.uuid();
    }

    public UUID getUuid() {
        return uuid;
    }
}
