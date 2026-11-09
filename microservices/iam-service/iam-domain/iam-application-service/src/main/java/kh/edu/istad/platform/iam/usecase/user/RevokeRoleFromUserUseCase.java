package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import kh.edu.istad.platform.iam.port.out.user.UserRoleAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RevokeRoleFromUserUseCase {

    private final UserRepository users;
    private final UserRoleAssignmentRepository assignments;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    @Transactional
    public void execute(UUID userUuid, UUID roleUuid, String reason) {

        var user = users.findByUuidForUpdate(userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("User", userUuid));

        var assignment = assignments
                .findByUserUuid(user.getUuid())
                .stream()
                .filter(value ->
                        value.getRoleUuid().equals(roleUuid) && value.getRevokedAt() == null
                )
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User-role assignment", roleUuid
                ));

        assignment.revoke(
                actorProvider.currentActor(),
                clock.instant(),
                reason
        );

        assignments.update(assignment);
    }
}