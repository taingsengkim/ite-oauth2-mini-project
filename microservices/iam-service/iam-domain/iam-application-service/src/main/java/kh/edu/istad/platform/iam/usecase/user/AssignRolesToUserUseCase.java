package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.dto.user.AssignRolesToUserCommand;
import kh.edu.istad.platform.iam.dto.user.RoleAssignmentResult;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import kh.edu.istad.platform.iam.mapper.user.UserApplicationMapper;
import kh.edu.istad.platform.iam.model.UserRoleAssignment;
import kh.edu.istad.platform.iam.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.port.out.role.RoleRepository;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import kh.edu.istad.platform.iam.port.out.user.UserRoleAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AssignRolesToUserUseCase {

    private final UserRepository users;
    private final RoleRepository roles;
    private final UserRoleAssignmentRepository assignments;
    private final CurrentActorProvider actorProvider;
    private final UserApplicationMapper mapper;
    private final Clock clock;

    @Transactional
    public List<RoleAssignmentResult> execute(AssignRolesToUserCommand command) {
        Objects.requireNonNull(command, "command");
        var roleUuids = command.roleUuids();

        if (roleUuids == null
                || roleUuids.isEmpty()
                || roleUuids.size() > 100
                || roleUuids.stream().anyMatch(Objects::isNull)
                || new HashSet<>(roleUuids).size() != roleUuids.size()) {

            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Provide 1 to 100 distinct role UUIDs");
        }

        var user = users.findByUuidForUpdate(command.userUuid())
                .orElseThrow(() -> new ResourceNotFoundException("User", command.userUuid()));

        var existing = assignments.findByUserUuid(user.getUuid());

        var now = clock.instant();
        var actor = actorProvider.currentActor();

        List<RoleAssignmentResult> results = new ArrayList<>();

        for (var roleUuid : roleUuids) {
            var role = roles.findByUuid(roleUuid)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", roleUuid));

            var assignment = UserRoleAssignment.assign(
                    user,
                    role,
                    actor,
                    now,
                    command.expiresAt(),
                    command.reason()
            );

            for (var previous : existing) {
                if (!previous.getRoleUuid().equals(roleUuid) || previous.getRevokedAt() != null) {
                    continue;
                }

                if (previous.isActive(now)) {
                    throw new ResourceConflictException("Role is already assigned to this user");
                }

                previous.revoke(actor, now, "Expired assignment replaced");
                assignments.update(previous);
            }

            var saved = assignments.create(assignment);
            results.add(mapper.toResult(saved));
        }

        return List.copyOf(results);
    }
}