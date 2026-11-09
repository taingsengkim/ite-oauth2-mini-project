package kh.edu.istad.platform.iam.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class IamOAuth2User extends DefaultOAuth2User {

    private final UUID userUuid;
    private final String localUsername;

    public IamOAuth2User(
            UUID userUuid,
            String localUsername,
            Collection<? extends GrantedAuthority> authorities,
            Map<String, Object> attributes,
            String nameAttributeKey
    ) {
        super(authorities, attributes, nameAttributeKey);

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