package kh.edu.istad.platform.iam.persistence.adapter.identity;

import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.model.Identity;
import kh.edu.istad.platform.iam.model.IdentityProvider;
import kh.edu.istad.platform.iam.persistence.mapper.identity.IdentityPersistenceMapper;
import kh.edu.istad.platform.iam.persistence.repository.identity.IdentityJpaRepository;
import kh.edu.istad.platform.iam.persistence.repository.user.UserJpaRepository;
import kh.edu.istad.platform.iam.port.out.identity.IdentityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IdentityPersistenceAdapter implements IdentityRepository {

    private final IdentityJpaRepository repository;
    private final UserJpaRepository users;
    private final IdentityPersistenceMapper mapper;

    @Override
    @Transactional
    public Identity create(Identity identity) {
        var owner = users
                .findByUuidAndDeletedFalse(identity.getUserUuid())
                .orElseThrow(() -> new ResourceNotFoundException("User", identity.getUserUuid()
                ));

        var entity = mapper.toNewEntity(identity, owner);

        try {
            return mapper.toDomain(repository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            throw translateConflict(exception);
        }
    }

    @Override
    public Optional<Identity> findByUuid(UUID uuid) {
        return repository.findByUuidAndDeletedFalse(uuid)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Identity> findByProviderAndSubject(
            IdentityProvider provider,
            String providerSubject
    ) {
        return repository
                .findByProviderAndProviderSubjectAndDeletedFalse(provider, providerSubject)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Identity> findByUserUuidAndProvider(
            UUID userUuid,
            IdentityProvider provider
    ) {
        return repository
                .findByUser_UuidAndProviderAndDeletedFalse(userUuid, provider)
                .map(mapper::toDomain);
    }

    @Override
    public List<Identity> findAllByUserUuid(UUID userUuid) {
        return repository
                .findAllByUser_UuidAndDeletedFalseOrderByLinkedAtAscIdAsc(userUuid)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void update(Identity identity) {
        var entity = repository
                .findByUuidAndDeletedFalse(identity.getUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Identity", identity.getUuid()));

        if (!entity.getUser().getUuid().equals(identity.getUserUuid())) {
            throw new ResourceConflictException("Identity owner cannot be changed");
        }

        mapper.updateEntity(identity, entity);

        try {
            repository.saveAndFlush(entity);
        } catch (OptimisticLockingFailureException exception) {
            throw new ResourceConflictException("Identity changed concurrently; reload and try again");
        } catch (DataIntegrityViolationException exception) {
            throw translateConflict(exception);
        }
    }

    private RuntimeException translateConflict(
            DataIntegrityViolationException exception
    ) {
        for (Throwable cause = exception;
             cause != null;
             cause = cause.getCause()) {

            String message = cause.getMessage();

            if (message == null) {
                continue;
            }

            if (message.contains(
                    "uk_identities_active_provider_subject"
            )) {
                return new ResourceConflictException(
                        "This external account is already linked"
                );
            }

            if (message.contains(
                    "uk_identities_active_user_provider"
            )) {
                return new ResourceConflictException(
                        "This user already has an account linked for this provider"
                );
            }
        }

        return exception;
    }
}