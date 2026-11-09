package kh.edu.istad.platform.iam.port.out.identity;

import kh.edu.istad.platform.iam.model.Identity;
import kh.edu.istad.platform.iam.model.IdentityProvider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdentityRepository {

    Identity create(Identity identity);

    Optional<Identity> findByUuid(UUID uuid);

    Optional<Identity> findByProviderAndSubject(
            IdentityProvider provider,
            String providerSubject
    );

    Optional<Identity> findByUserUuidAndProvider(
            UUID userUuid,
            IdentityProvider provider
    );

    List<Identity> findAllByUserUuid(UUID userUuid);

    void update(Identity identity);
}
