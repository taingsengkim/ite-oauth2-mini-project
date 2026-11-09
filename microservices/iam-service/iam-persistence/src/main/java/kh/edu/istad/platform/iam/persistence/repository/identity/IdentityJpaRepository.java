package kh.edu.istad.platform.iam.persistence.repository.identity;

import kh.edu.istad.platform.iam.model.IdentityProvider;
import kh.edu.istad.platform.iam.persistence.entity.identity.IdentityEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdentityJpaRepository
        extends JpaRepository<IdentityEntity, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<IdentityEntity> findByUuidAndDeletedFalse(UUID uuid);

    @EntityGraph(attributePaths = "user")
    Optional<IdentityEntity>
    findByProviderAndProviderSubjectAndDeletedFalse(
            IdentityProvider provider,
            String providerSubject
    );

    @EntityGraph(attributePaths = "user")
    Optional<IdentityEntity>
    findByUser_UuidAndProviderAndDeletedFalse(
            UUID userUuid,
            IdentityProvider provider
    );

    @EntityGraph(attributePaths = "user")
    List<IdentityEntity>
    findAllByUser_UuidAndDeletedFalseOrderByLinkedAtAscIdAsc(
            UUID userUuid
    );
}
