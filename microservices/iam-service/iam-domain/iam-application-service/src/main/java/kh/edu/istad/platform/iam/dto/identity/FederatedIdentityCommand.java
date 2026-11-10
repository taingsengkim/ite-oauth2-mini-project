
package kh.edu.istad.platform.iam.dto.identity;

import kh.edu.istad.platform.iam.model.IdentityProvider;

import java.util.Map;
import java.io.Serializable;

public record FederatedIdentityCommand(
        IdentityProvider provider,
        String providerSubject,
        String email,
        boolean emailVerified,
        String displayName,
        String username,
        String pictureUrl,
        String givenName,
        String familyName,
        String locale,
        Map<String, Object> attributes
) implements Serializable {
    public FederatedIdentityCommand {
        attributes = attributes == null ? Map.of() : java.util.Collections.unmodifiableMap(
                new java.util.LinkedHashMap<>(attributes));
    }
    @Override
    public String toString() { return "FederatedIdentityCommand[provider=" + provider + "]"; }
}
