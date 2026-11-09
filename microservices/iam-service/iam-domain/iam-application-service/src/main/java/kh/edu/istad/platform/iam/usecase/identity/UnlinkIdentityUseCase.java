package kh.edu.istad.platform.iam.usecase.identity;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import kh.edu.istad.platform.iam.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.port.out.identity.IdentityRepository;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UnlinkIdentityUseCase {

    private final UserRepository users;
    private final IdentityRepository identities;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    @Transactional
    public void execute(UUID userUuid, UUID identityUuid) {
        Objects.requireNonNull(userUuid, "userUuid");
        Objects.requireNonNull(identityUuid, "identityUuid");

        var user = users.findByUuidForUpdate(userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("User", userUuid));

        var identity = identities.findByUuid(identityUuid)
                .filter(value -> value.getUserUuid().equals(userUuid))
                .orElseThrow(() -> new ResourceNotFoundException("Identity", identityUuid));

        boolean hasLocalPassword = user.getPasswordHash() != null
                && !user.getPasswordHash().isBlank();

        boolean hasAnotherIdentity = identities
                .findAllByUserUuid(userUuid)
                .stream()
                .anyMatch(value -> !value.getUuid().equals(identityUuid));

        if (!hasLocalPassword && !hasAnotherIdentity) {
            throw new IamDomainException(IamDomainException.Code.RULE_VIOLATION, "Cannot unlink the last sign-in method");
        }

        identity.delete(
                actorProvider.currentActor(),
                clock.instant()
        );

        identities.update(identity);
    }
}