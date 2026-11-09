package kh.edu.istad.platform.iam.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public final class IamOidcUser extends DefaultOidcUser {

    private final UUID userUuid;
    private final String localUsername;

    public IamOidcUser(
            UUID userUuid,
            String localUsername,
            Collection<? extends GrantedAuthority> authorities,
            OidcUser providerUser
    ) {
        super(
                authorities,
                providerUser.getIdToken(),
                providerUser.getUserInfo()
        );

        this.userUuid = Objects.requireNonNull(userUuid, "userUuid");
        this.localUsername = Objects.requireNonNull(
                localUsername, "localUsername"
        );
    }

    public UUID getUserUuid() {
        return userUuid;
    }

    @Override
    public String getName() {
        return localUsername;
    }
}
