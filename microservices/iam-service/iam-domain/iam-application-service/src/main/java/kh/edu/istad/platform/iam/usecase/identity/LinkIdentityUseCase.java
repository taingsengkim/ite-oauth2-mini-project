package kh.edu.istad.platform.iam.usecase.identity;

import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.dto.identity.FederatedIdentityCommand;
import kh.edu.istad.platform.iam.dto.identity.IdentityResult;
import kh.edu.istad.platform.iam.mapper.identity.IdentityApplicationMapper;
import kh.edu.istad.platform.iam.model.Identity;
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
public class LinkIdentityUseCase {

    private final UserRepository users;
    private final IdentityRepository identities;
    private final IdentityApplicationMapper mapper;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    @Transactional
    public IdentityResult execute(
            UUID userUuid,
            FederatedIdentityCommand command
    ) {
        Objects.requireNonNull(userUuid, "userUuid");
        Objects.requireNonNull(command, "command");

        users.findByUuidForUpdate(userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("User", userUuid));

        var identity = Identity.create(
                userUuid,
                command.provider(),
                command.providerSubject(),
                command.email(),
                command.emailVerified(),
                command.displayName(),
                command.username(),
                command.pictureUrl(),
                command.attributes(),
                actorProvider.currentActor(),
                clock.instant()
        );

        if (identities.findByProviderAndSubject(
                identity.getProvider(),
                identity.getProviderSubject()
        ).isPresent()) {
            throw new ResourceConflictException("This external account is already linked");
        }

        if (identities.findByUserUuidAndProvider(
                userUuid,
                identity.getProvider()
        ).isPresent()) {
            throw new ResourceConflictException("This user already has an account linked for this provider");
        }

        return mapper.toResult(identities.create(identity));
    }
}
