package kh.edu.istad.platform.iam.usecase.role;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.model.Role;
import kh.edu.istad.platform.iam.port.out.*;
import kh.edu.istad.platform.iam.port.out.role.RolePermissionAssignmentRepository;
import kh.edu.istad.platform.iam.port.out.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RevokePermissionFromRoleUseCase {
    private final RoleRepository repository;
    private final RolePermissionAssignmentRepository assignments;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;
    @Transactional
    public void execute(UUID roleUuid, UUID permissionUuid, String reason) {
        Role role = repository.findByUuidForUpdate(roleUuid).orElseThrow(() -> new ResourceNotFoundException("Role", roleUuid));
        role.requireMutable();
        var assignment = assignments.findByRoleUuid(roleUuid).stream()
                .filter(value -> value.getPermissionUuid().equals(permissionUuid) && value.getRevokedAt() == null)
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("Role permission assignment", permissionUuid));
        assignment.revoke(actorProvider.currentActor(), clock.instant(), reason);
        assignments.update(assignment);
    }
}
