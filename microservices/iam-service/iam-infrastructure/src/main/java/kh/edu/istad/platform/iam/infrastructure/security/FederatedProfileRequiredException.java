package kh.edu.istad.platform.iam.infrastructure.security;

import kh.edu.istad.platform.iam.dto.identity.FederatedIdentityCommand;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

public final class FederatedProfileRequiredException extends OAuth2AuthenticationException {
    private final String registrationId;
    private final FederatedIdentityCommand identity;

    public FederatedProfileRequiredException(String registrationId, FederatedIdentityCommand identity) {
        super(new OAuth2Error("profile_completion_required"));
        this.registrationId = registrationId;
        this.identity = identity;
    }

    public String getRegistrationId() { return registrationId; }
    public FederatedIdentityCommand getIdentity() { return identity; }
}
