
package kh.edu.istad.platform.iam.dto.identity;

import kh.edu.istad.platform.iam.model.IdentityProvider;

import java.util.Map;

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
) {
}
