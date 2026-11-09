package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.dto.user.GrantPermissionsToUserCommand;
import kh.edu.istad.platform.iam.dto.user.PermissionGrantResult;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import kh.edu.istad.platform.iam.mapper.user.UserApplicationMapper;
import kh.edu.istad.platform.iam.model.UserPermissionAssignment;
import kh.edu.istad.platform.iam.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.port.out.permission.PermissionRepository;
import kh.edu.istad.platform.iam.port.out.user.UserPermissionAssignmentRepository;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
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
public class GrantPermissionsToUserUseCase {

    private final UserRepository users;
    private final PermissionRepository permissions;
    private final UserPermissionAssignmentRepository assignments;
    private final CurrentActorProvider actorProvider;
    private final UserApplicationMapper mapper;
    private final Clock clock;

    @Transactional
    public List<PermissionGrantResult> execute(
            GrantPermissionsToUserCommand command) {

        Objects.requireNonNull(command, "command");

        var permissionUuids = command.permissionUuids();

        if (permissionUuids == null
                || permissionUuids.isEmpty()
                || permissionUuids.size() > 100
                || permissionUuids.stream().anyMatch(Objects::isNull)
                || new HashSet<>(permissionUuids).size()
                != permissionUuids.size()) {

            throw new IamDomainException(
                    IamDomainException.Code.VALIDATION_ERROR,
                    "Provide 1 to 100 distinct permission UUIDs"
            );
        }

        if (command.effect() == null) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Permission effect is required"
            );
        }

        var user = users.findByUuidForUpdate(command.userUuid())
                .orElseThrow(() -> new ResourceNotFoundException("User", command.userUuid()
                ));

        var existing = assignments.findByUserUuid(user.getUuid());
        var now = clock.instant();
        var actor = actorProvider.currentActor();

        List<PermissionGrantResult> results = new ArrayList<>();

        for (var permissionUuid : permissionUuids) {
            var permission = permissions.findByUuid(permissionUuid)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Permission", permissionUuid
                    ));

            var assignment = UserPermissionAssignment.assign(
                    user,
                    permission,
                    command.effect(),
                    actor,
                    now,
                    command.expiresAt(),
                    command.reason()
            );

            for (var previous : existing) {
                if (!previous.getPermissionUuid().equals(permissionUuid)
                        || previous.getRevokedAt() != null) {
                    continue;
                }

                boolean active = previous.isActive(now);

                if (active && previous.getEffect() == command.effect()) {
                    throw new ResourceConflictException("Permission already has this effect for the user"
                    );
                }

                previous.revoke(actor, now, active
                                ? "Permission effect changed"
                                : "Expired assignment replaced"
                );

                assignments.update(previous);
            }

            var saved = assignments.create(assignment);
            results.add(mapper.toResult(saved));
        }

        return List.copyOf(results);
    }
}
